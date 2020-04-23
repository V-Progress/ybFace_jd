# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\sdk2.3/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# 设置混淆的压缩比率 0 ~ 7
-optimizationpasses 5
# 混淆后类名都为小写   Aa aA
-dontusemixedcaseclassnames
# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
#不做预校验的操作
#-dontpreverify
# 混淆时不记录日志
#-verbose
# 混淆采用的算法.
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#保留代码行号，方便异常信息的追踪
-keepattributes SourceFile,LineNumberTable

#dump文件列出apk包内所有class的内部结构
-dump class_files.txt
#seeds.txt文件列出未混淆的类和成员
-printseeds seeds.txt
#usage.txt文件列出从apk中删除的代码
-printusage unused.txt
#mapping文件列出混淆前后的映射
-printmapping mapping.txt

#避免混淆Android基本组件，下面是兼容性比较高的规则
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep class com.google.android.material.**{*;}

#不提示V4包下错误警告
-dontwarn android.support.v4.**
#保持下面的V4兼容包的类不被混淆
-keep class android.support.v4.**{*;}
#避免混淆所有native的方法,涉及到C、C++
-keepclasseswithmembernames class * {
        native <methods>;
}
#避免混淆自定义控件类的get/set方法和构造函数
-keep public class * extends android.view.View{
        *** get*();
        void set*(***);
        public <init>(android.content.Context);
        public <init>(android.content.Context,android.util.AttributeSet);
        public <init>(android.content.Context,android.util.AttributeSet,int);
}
#避免混淆枚举类
-keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
}

#不混淆Parcelable和它的实现子类，还有Creator成员变量
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#避免混淆js相关的接口
-keepattributes *JavascriptInterface*
#不混淆Serializable和它的实现子类、其成员变量
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

 # ===============百度定位 start====================
-keep class vi.com.gdi.** { *; }
-keep public class com.baidu.** {*;}
-keep public class com.mobclick.** {*;}
-dontwarn com.baidu.mapapi.utils.*
-dontwarn com.baidu.platform.comapi.b.*
-dontwarn com.baidu.platform.comapi.map.*
# ===============百度定位 end======================

# ==================bugly start==================
-dontwarn com.tencent.bugly.**
-keep public interface com.tencent.**
-keep public class com.tencent.** {*;}
-keep public class com.tencent.bugly.**{*;}
# ==================bugly end====================

# ==================EventBus start=================
-keep class org.greenrobot.** {*;}
-keep class de.greenrobot.** {*;}
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}
# ==================EventBus end===================

# ==================okhttp start===================
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-dontwarn okio.**
-keep class okio.**{*;}
-keep interface okio.**{*;}
# ==================okhttp end=====================

#避免混淆Rxjava/RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-keep class com.umeng.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

### greenDAO 3
-dontwarn org.greenrobot.greendao.**
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

-dontwarn org.apache.commons.logging.**
-ignorewarnings
-keep class * {
public private *;
}

-keep class com.yunbiao.ybsmartcheckin_live_id.bean.**{*;}

# Androidx混淆配置
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

-keep class android.support.**{*;}