# SPASC

Legacy Xposed module for Salt Player Artist Separator Customization.

椒盐音乐 自定义艺术家分隔符 Xposed 模块

需要勾选 椒盐音乐(com.salt.music)，理论全版本，只测试了最近的内部预览版

保存后将会尝试清空椒盐音乐已缓存的艺术家数据库，需要重启椒盐音乐并刷新音乐库后生效，不会修改和丢失歌曲信息

|软件内截图|模块截图|
|--|--|
| <img src="pics\example01.jpg" height="300" alt="screenshot"> | <img src="pics\module.jpg" height="300" alt="screenshot"> |

## Build

```sh
./gradlew testDebugUnitTest assembleDebug
```

On Windows:

```bat
gradlew.bat testDebugUnitTest assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```
