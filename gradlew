#!/bin/sh
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
exec java -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
```

5. 點 **Commit changes → Commit changes**

---

## 建立 gradle-wrapper.properties

1. 點 **Add file → Create new file**
2. 檔名填：`gradle/wrapper/gradle-wrapper.properties`
   （直接打這整串，GitHub 會自動建資料夾）
3. 貼入以下內容：
```
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
