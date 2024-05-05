# USM Audio chunks rewriter
Rewrite audio chunks in USM files in one folder, use USM audio chunks from files with ones name in another folder.
# How to use?
1. You need [java](https://www.java.com/download/) on you computer.
2. Download [USM_Audio_chunks_rewriter.jar](https://github.com/guzenco/USM_Audio_chunks_rewriter/releases/).
3. Open console in folder with USM_Audio_chunks_rewriter.jar.
4. Start program with command
```
java -jar USM_Audio_chunks_rewriter.jar
```
5. Write path to .usm files for rewriting
6. Write path to .usm files with audio
7. Confirm start
8. Wait until "DONE!".

You can see the use case for CGs in Honkai Impact 3 [here](https://youtu.be/rL4uFQeLoLI).
# Command line arguments

``-group-size=<n>``

&emsp;Represents the size of groups of audio chunks evenly distributed throughout the file. Typically should be equal to the number of audio tracks. A value greater than the number of audio tracks can help with or cause stutters/freezing in some players.

&emsp;Default: ``1``

&emsp;Valid values: ``[1, ∞)``

&emsp;Example: ``java -jar USM_Audio_chunks_rewriter.jar -group-size=2``
