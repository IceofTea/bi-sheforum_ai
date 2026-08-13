import os
import json
import requests
import numpy as np
from config import (
    CHROMA_PERSIST_DIR, COLLECTION_POSTS, COLLECTION_TITLES,
    EMBEDDING_BACKEND, EMBEDDING_MODEL, EMBEDDING_DIMENSION,
    OLLAMA_BASE_URL, TOP_K, SIMILARITY_THRESHOLD,
    RECOMMEND_TOP_K, RECOMMEND_THRESHOLD
)

try:
    import chromadb
    from chromadb.config import Settings
    CHROMADB_AVAILABLE = True
except ImportError:
    CHROMADB_AVAILABLE = False

try:
    from sentence_transformers import SentenceTransformer
    SENTENCE_TRANSFORMERS_AVAILABLE = True
except ImportError:
    SENTENCE_TRANSFORMERS_AVAILABLE = False


class EmbeddingEngine:
    def __init__(self):
        print(f"[Embedding] 初始化引擎 (后端: {EMBEDDING_BACKEND}, 模型: {EMBEDDING_MODEL})")
        self._model = None
        self._init_embedding_model()
        self._init_vector_store()

    # ── 嵌入模型初始化 ──────────────────────────

    def _init_embedding_model(self):
        if EMBEDDING_BACKEND == "ollama":
            self._check_ollama()
        elif EMBEDDING_BACKEND == "sentence-transformers":
            self._init_sentence_transformers()
        elif EMBEDDING_BACKEND == "api":
            print("[Embedding] 使用 API 嵌入模式（需在子类中实现 _call_api_embedding）")
        else:
            print(f"[Embedding] 未知后端 {EMBEDDING_BACKEND}，回退到 Ollama")
            self._check_ollama()

    def _check_ollama(self):
        try:
            resp = requests.get(f"{OLLAMA_BASE_URL}/api/tags", timeout=5)
            models = [m["name"] for m in resp.json().get("models", [])]
            if not any(EMBEDDING_MODEL in m for m in models):
                print(f"[Embedding] 警告: Ollama 中未找到模型 '{EMBEDDING_MODEL}'")
                print(f"           请运行: ollama pull {EMBEDDING_MODEL}")
            else:
                print(f"[Embedding] Ollama 已就绪，可用嵌入模型: {[m for m in models if EMBEDDING_MODEL in m]}")
        except Exception as e:
            print(f"[Embedding] Ollama 连接失败: {e}")
            print(f"[Embedding] 请确保 Ollama 已安装并运行在 {OLLAMA_BASE_URL}")

    def _init_sentence_transformers(self):
        if SENTENCE_TRANSFORMERS_AVAILABLE:
            try:
                self._model = SentenceTransformer(EMBEDDING_MODEL)
                print(f"[Embedding] sentence-transformers 模型 '{EMBEDDING_MODEL}' 加载成功")
            except Exception as e:
                print(f"[Embedding] sentence-transformers 加载失败: {e}")
                self._model = None
        else:
            print("[Embedding] sentence-transformers 未安装，回退到 Ollama")
            self._check_ollama()

    # ── 向量计算 ──────────────────────────────

    def embed(self, text: str) -> list:
        if not text or not text.strip():
            return [0.0] * EMBEDDING_DIMENSION

        if EMBEDDING_BACKEND == "ollama":
            return self._embed_ollama(text)
        elif EMBEDDING_BACKEND == "sentence-transformers" and self._model is not None:
            return self._embed_sentence(text)
        else:
            return self._embed_ollama(text)

    def _embed_ollama(self, text: str) -> list:
        try:
            resp = requests.post(
                f"{OLLAMA_BASE_URL}/api/embeddings",
                json={"model": EMBEDDING_MODEL, "prompt": text},
                timeout=30
            )
            if resp.status_code == 200:
                return resp.json().get("embedding", [0.0] * EMBEDDING_DIMENSION)
        except Exception as e:
            print(f"[Embedding] Ollama 嵌入失败: {e}")
        return [0.0] * EMBEDDING_DIMENSION

    def _embed_sentence(self, text: str) -> list:
        try:
            vec = self._model.encode(text, normalize_embeddings=True)
            return vec.tolist()
        except Exception as e:
            print(f"[Embedding] sentence-transformers 编码失败: {e}")
            return [0.0] * EMBEDDING_DIMENSION

    def embed_batch(self, texts: list) -> list:
        return [self.embed(t) for t in texts]

    # ── 向量存储初始化 ─────────────────────────

    def _init_vector_store(self):
        if not CHROMADB_AVAILABLE:
            print("[Embedding] ChromaDB 未安装，无法使用向量存储")
            print("[Embedding] 请运行: pip install chromadb>=0.4.0")
            self.collection = None
            self.title_collection = None
            return

        os.makedirs(CHROMA_PERSIST_DIR, exist_ok=True)
        try:
            self.client = chromadb.PersistentClient(
                path=CHROMA_PERSIST_DIR,
                settings=Settings(anonymized_telemetry=False)
            )
            self.collection = self.client.get_or_create_collection(
                name=COLLECTION_POSTS,
                metadata={"description": "论坛帖子内容向量"}
            )
            self.title_collection = self.client.get_or_create_collection(
                name=COLLECTION_TITLES,
                metadata={"description": "论坛帖子标题向量（用于关联推荐）"}
            )
            print(f"[Embedding] ChromaDB 已就绪")
            print(f"           帖子内容集合: {self.collection.count()} 条")
            print(f"           帖子标题集合: {self.title_collection.count()} 条")
        except Exception as e:
            print(f"[Embedding] ChromaDB 初始化失败: {e}")
            self.collection = None
            self.title_collection = None

    # ── 索引管理 ──────────────────────────────

    def add_post(self, post_id: int, content: str, title: str = "", writer: str = "", url: str = ""):
        """索引一个帖子（内容+标题分别存储）"""
        if self.collection is None:
            print("[Embedding] ChromaDB 未就绪，跳过索引")
            return

        post_id_str = str(post_id)
        content = content.strip()
        title = title.strip()

        # 帖子内容（用于语义检索）
        if content:
            content_embedding = self.embed(content)
            self.collection.upsert(
                ids=[f"content_{post_id_str}"],
                embeddings=[content_embedding],
                documents=[content],
                metadatas=[{
                    "post_id": post_id,
                    "type": "content",
                    "title": title,
                    "writer": writer,
                    "url": url
                }]
            )

        # 帖子标题（用于关联推荐）
        if title:
            title_embedding = self.embed(title)
            self.title_collection.upsert(
                ids=[f"title_{post_id_str}"],
                embeddings=[title_embedding],
                documents=[title],
                metadatas=[{
                    "post_id": post_id,
                    "type": "title",
                    "writer": writer,
                    "url": url
                }]
            )

    def add_comment(self, comment_id: int, thread_id: int, content: str, user_id: int = 0):
        """索引一条评论（每个楼层作为一个chunk）"""
        if self.collection is None:
            return

        content = content.strip()
        if not content:
            return

        cid = f"comment_{comment_id}"
        content_embedding = self.embed(content)
        self.collection.upsert(
            ids=[cid],
            embeddings=[content_embedding],
            documents=[content],
            metadatas=[{
                "post_id": thread_id,
                "type": "comment",
                "comment_id": comment_id,
                "user_id": user_id
            }]
        )

    # ── 语义检索 ──────────────────────────────

    def search(self, query: str, top_k: int = None) -> list:
        """语义检索最相似的帖子内容"""
        if self.collection is None:
            return []
        if not query or not query.strip():
            return []

        top_k = top_k or TOP_K
        query_embedding = self.embed(query)

        try:
            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k,
                include=["documents", "metadatas", "distances"]
            )
            return self._format_results(results)
        except Exception as e:
            print(f"[Embedding] 检索失败: {e}")
            return []

    def get_similar_posts(self, query: str, threshold: float = None) -> list:
        """获取高于阈值的相似帖子"""
        threshold = threshold if threshold is not None else SIMILARITY_THRESHOLD
        results = self.search(query, top_k=10)
        return [r for r in results if r["similarity"] >= threshold]

    def get_related_questions(self, query: str, top_k: int = None, threshold: float = None) -> list:
        """关联问题推荐：在标题集合中检索语义相似的历史问题"""
        if self.title_collection is None:
            return []

        top_k = top_k or RECOMMEND_TOP_K
        threshold = threshold if threshold is not None else RECOMMEND_THRESHOLD
        query_embedding = self.embed(query)

        try:
            results = self.title_collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k + 1,
                include=["documents", "metadatas", "distances"]
            )
            formatted = self._format_results(results)
            return [r for r in formatted if r["similarity"] >= threshold]
        except Exception as e:
            print(f"[Embedding] 关联推荐检索失败: {e}")
            return []

    def _format_results(self, results: dict) -> list:
        if not results or not results.get("ids") or not results["ids"][0]:
            return []

        formatted = []
        for i in range(len(results["ids"][0])):
            dist = results["distances"][0][i] if results.get("distances") else 0
            formatted.append({
                "id": results["ids"][0][i],
                "content": results["documents"][0][i] if results.get("documents") else "",
                "similarity": float(1 - dist),
                "metadata": results["metadatas"][0][i] if results.get("metadatas") else {}
            })
        return formatted

    # ── 维护 ──────────────────────────────────

    def delete_post(self, post_id: int):
        if self.collection is None:
            return
        pid = str(post_id)
        try:
            self.collection.delete(ids=[f"content_{pid}"])
            self.title_collection.delete(ids=[f"title_{pid}"])
        except Exception:
            pass

    def reset(self):
        if self.collection is None:
            return
        try:
            self.client.delete_collection(COLLECTION_POSTS)
            self.client.delete_collection(COLLECTION_TITLES)
        except Exception:
            pass
        self.collection = self.client.get_or_create_collection(name=COLLECTION_POSTS)
        self.title_collection = self.client.get_or_create_collection(name=COLLECTION_TITLES)
        print("[Embedding] 索引已重置")

    @property
    def stats(self) -> dict:
        return {
            "posts": self.collection.count() if self.collection else 0,
            "titles": self.title_collection.count() if self.title_collection else 0,
            "backend": EMBEDDING_BACKEND,
            "model": EMBEDDING_MODEL
        }
