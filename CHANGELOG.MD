- [0.3.0](https://github.com/DavixDevelop/terracustomtreerepo/releases/tag/0.3.0)
  - Compatibility bump to TerraPlusPlus 1.1.0.639+
  - Update to ForgeGradle 5.1+, Gradle 7.2 and Forge 14.23.5.2860

  >**Release note:** Use this version for post TerraPlusPlus 1.1.0.639 releases and use **Forge 14.23.5.2860**
- [0.2.5](https://github.com/DavixDevelop/terracustomtreerepo/releases/tag/0.2.5)
  - Compatibility bump to TerraPlusPlus 1.1.0.627+ (Because of update to PorkLib)
  - Changed versioning to `0.2.5.[GitHub commit count]`
  - Fixed issue with the addon making the server unresponsive, blocking terrain generation or both. The issue lied within the `SegmentsBaker:requestData` method, which is a non-blocking method. <br>To be more precise the `requestData` method was first making a request to get the tree cover data. After requesting the data, it tried to read the data withing the same method (via `CompletableFuture<T>:join`), therefore blocking the thread, and sequentially blocking new chunks from generating.

  >**Release note:** Use this version for post TerraPlusPlus 1.1.0.627 releases
- [0.2.4](https://github.com/DavixDevelop/terracustomtreerepo/releases/tag/0.2.4)
  - Removed builtin datasets and switched to a [common library mod](https://github.com/DavixDevelop/terracommondatasets)
  - Fixed issue with a dependency of ForgeGradle being compiled against JDK 11 (and not JDK 8), by switching to the https://maven.daporkchop.net repository
  - Fixed wrong index (227-> 127) to tree in tree_meta csv file
  - Version number now includes commit count for easier management
  - Removed jcenter

  >**Release note:** Use this version for pre TerraPlusPlus 1.1.0.627 releases
- [0.2.3](https://github.com/DavixDevelop/terracustomtreerepo/releases/tag/0.2.3)
  - Fixed an error in the Custom Tree Repository that caused crashes
  - Removed old precompiled low-resolution CSV based treemap
  - Implemented higher resolution Koppen climate dataset        
    Implemented moderate resolution continents dataset
  - Implemented an on-the-fly treemap with a custom tree baker, based on the two aforementioned dataset

  >**Release note:** It's not recommended using this version combined with the new Terra++ Improved Biomes addon as of right now, due to the new biomes (ex. Dark Forest)
- [0.2.2](https://github.com/DavixDevelop/terracustomtreerepo/releases/tag/0.2.2)
  - Compatibility patch for TerraPlusPlus 0.1.522+
- [0.2](https://github.com/DavixDevelop/terracustomtreerepo/releases/tag/0.2)
  - Beta release
  - Updated to reflect recent merge from fast-osm branch
  - Increased tree density and further fixes
  - Further, increased density, further randomized trees and changed limit for small and large trees