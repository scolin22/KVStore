Colin Stone 31645112

Port: 45112

Run: java -Xmx64m -classpath KVStore.jar com.scolin22.cpen431.A3.Main 45112

206.12.16.154:45112
142.103.2.2:45112

The design mostly follows the server implmented for A2 with the exception of a ccaching layer for at-most-once behaviour.
The caching layer is implemented using the Cache interface provided by the Google Guava library. This has an advantage to using ConcurrentMap as a ConcurrentMap persists all elements that are added to it until they are explicityly removed. A Cache can be configured to evict entries automatically. It is extremely convenient to implement a fixed size LRU cache with TTL for cache entries using Cache.
