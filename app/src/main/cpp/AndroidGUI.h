//
// Created by matthew on 06/07/2020.
//

#ifndef MY_APPLICATION_ANDROIDGUI_H
#define MY_APPLICATION_ANDROIDGUI_H

#include "GUI.h"
#include "../../../../../../Library/Android/sdk/ndk/21.0.6113669/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/jni.h"
#include <chrono>
#include <oboe/Oboe.h>
#include <GPU.h>

class AndroidGUI : GUI {
private:
    JNIEnv *env;
    jobject surfaceView;
    std::chrono::steady_clock::time_point lastFrame = std::chrono::steady_clock::now();
    std::chrono::steady_clock::time_point renderTimer = std::chrono::steady_clock::now();
    unsigned short renderFrameCount = 0;
    oboe::ManagedStream managedStream;
    uint32 pixels[SCREEN_WIDTH * SCREEN_HEIGHT];
    bool buttonsDown[8] = {[0 ... 7] = false};
public:
    unsigned short fps = 0;
    unsigned short renderFps = 0;
    bool running = false;

    AndroidGUI(JNIEnv *env, jobject surfaceView);
    void render(JNIEnv* env);
    void setButtonState(uint8 button, bool state);

    void displayBuffer(uint32 *pixels) override;
    void displayFPS(uint16 fps) override;
    bool isDown(uint8 button) override;
    bool isOpen() override;
private:
    void playAudio(float *samples, uint16 count) override;
};


#endif //MY_APPLICATION_ANDROIDGUI_H
