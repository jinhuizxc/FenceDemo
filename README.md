# FenceDemo
围栏Demo

## bug
> Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'com.amap.api.maps.UiSettings com.amap.api.maps.AMap.getUiSettings()' on a null object reference

解决办法就是在build.gradle里面加上

> sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
