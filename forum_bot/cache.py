import time
import threading
from config import REDIS_HOST, REDIS_PORT, REDIS_DB, REDIS_TTL

try:
    import redis as redis_lib
    REDIS_AVAILABLE = True
except ImportError:
    REDIS_AVAILABLE = False


class MemoryCache:
    """线程安全的内存缓存（Redis 不可用时的兜底）"""

    def __init__(self, default_ttl=3600):
        self._store = {}
        self._ttl = {}
        self._lock = threading.Lock()
        self.default_ttl = default_ttl

    def get(self, key: str):
        with self._lock:
            if key in self._store:
                if time.time() < self._ttl[key]:
                    return self._store[key]
                else:
                    del self._store[key]
                    del self._ttl[key]
            return None

    def set(self, key: str, value, ttl: int = None):
        with self._lock:
            self._store[key] = value
            self._ttl[key] = time.time() + (ttl or self.default_ttl)

    def delete(self, key: str):
        with self._lock:
            self._store.pop(key, None)
            self._ttl.pop(key, None)

    def clear(self):
        with self._lock:
            self._store.clear()
            self._ttl.clear()

    @property
    def size(self):
        return len(self._store)


class RedisCache:
    """Redis 缓存，带内存兜底"""

    def __init__(self):
        self.default_ttl = REDIS_TTL
        self._redis = None
        self._memory = MemoryCache(default_ttl=REDIS_TTL)

        if REDIS_AVAILABLE:
            try:
                self._redis = redis_lib.Redis(
                    host=REDIS_HOST,
                    port=REDIS_PORT,
                    db=REDIS_DB,
                    decode_responses=True,
                    socket_timeout=3
                )
                self._redis.ping()
                print(f"[Cache] Redis 已连接 ({REDIS_HOST}:{REDIS_PORT})")
            except Exception as e:
                print(f"[Cache] Redis 连接失败: {e}")
                print(f"[Cache] 使用内存缓存作为兜底")
                self._redis = None
        else:
            print("[Cache] redis 库未安装，使用内存缓存")
            print("        如需 Redis 请运行: pip install redis")

    def get(self, key: str):
        if self._redis is not None:
            try:
                return self._redis.get(key)
            except Exception:
                pass
        return self._memory.get(key)

    def set(self, key: str, value, ttl: int = None):
        ttl = ttl or self.default_ttl
        if self._redis is not None:
            try:
                self._redis.setex(key, ttl, value)
                return
            except Exception:
                pass
        self._memory.set(key, value, ttl)

    def delete(self, key: str):
        if self._redis is not None:
            try:
                self._redis.delete(key)
                return
            except Exception:
                pass
        self._memory.delete(key)

    def clear(self):
        if self._redis is not None:
            try:
                self._redis.flushdb()
                return
            except Exception:
                pass
        self._memory.clear()

    @property
    def is_redis_connected(self) -> bool:
        if self._redis is not None:
            try:
                return self._redis.ping()
            except Exception:
                return False
        return False
