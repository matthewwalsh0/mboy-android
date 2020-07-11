//
// Created by matthew on 06/07/2020.
//

#ifndef MY_APPLICATION_ANDROIDGUI_H
#define MY_APPLICATION_ANDROIDGUI_H

#include "GUI.h"
#include "../../../../../../Library/Android/sdk/ndk/21.0.6113669/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/jni.h"
#include <chrono>

class AndroidGUI : GUI {
private:
    JNIEnv *env;
    jobject surfaceView;
    std::chrono::steady_clock::time_point lastFrame = std::chrono::steady_clock::now();
    std::chrono::steady_clock::time_point renderTimer = std::chrono::steady_clock::now();
    unsigned short renderFrameCount = 0;
public:
    unsigned short fps = 0;
    unsigned short renderFps = 0;

    AndroidGUI(JNIEnv *env, jobject surfaceView);
    void displayBuffer(uint32 *pixels);
    void displayFPS(uint16 fps);
};


#endif //MY_APPLICATION_ANDROIDGUI_H