# SaltSplit

Legacy Xposed module for Salt Player artist splitting.

椒盐音乐 自定义艺术家分隔符 Xposed 模块

需要勾选 椒盐音乐(com.salt.music)，理论全版本，只测试了最近的内部预览版

保存后将会尝试清空椒盐音乐已缓存的艺术家数据库，需要重启椒盐音乐并刷新音乐库后生效，不会修改和丢失歌曲信息

|软件内截图|模块截图|
|--|--|
| <img src="pics\example01.jpg" height="300" alt="screenshot"> | <img src="pics\module.jpg" height="300" alt="screenshot"> |

提了几次建议作者大大都没理，不敢问了...

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

## Release

GitHub Actions 的 release workflow 只能手动触发，版本号只填 `x.x.x`，例如 `1.2.3`。

发布前需要在仓库 Settings -> Secrets and variables -> Actions 配置这些 secrets：

- `RELEASE_KEYSTORE_BASE64`: release keystore 文件的 Base64 内容
- `RELEASE_KEYSTORE_PASSWORD`: keystore 密码
- `RELEASE_KEY_ALIAS`: key alias
- `RELEASE_KEY_PASSWORD`: key 密码

workflow 会自动生成 `vX.X.X` tag、release APK 和 `update.json`。
