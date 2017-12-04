/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "termios.h"
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>
#include <strings.h>

#include "com_black_serialport_SerialPort.h"

#include "android/log.h"

#define TAG "serial_port"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#define LOGW(fmt, args...) __android_log_print(ANDROID_LOG_WARN, TAG, fmt, ##args)

static int mTtyfd = -1;

static speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_com_black_serialport_SerialPort_open
        (JNIEnv *env, jclass thiz, jstring path) {
    jobject mFileDescriptor;

    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | O_NONBLOCK);
        mTtyfd = open(path_utf, O_RDWR | O_NONBLOCK);
        LOGD("open() fd = %d", mTtyfd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (mTtyfd == -1) {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
        mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint) mTtyfd);
    }

    return mFileDescriptor;
}

/*
 * Class:     com_black_serialport_SerialPort
 * Method:    setConfig
 * Signature: (ICII)I
 */
JNIEXPORT jint JNICALL Java_com_black_serialport_SerialPort_setConfig
        (JNIEnv *env, jobject thiz, jint nBits, jchar nEvent, jint nSpeed, jint nStop) {
    LOGW("set_opt:nBits=%d,nEvent=%c,nSpeed=%d,nStop=%d", nBits, nEvent, nSpeed, nStop);
    struct termios newtio, oldtio;
    if (tcgetattr(mTtyfd, &oldtio) != 0) {
        LOGE("setup serial failure");
        return -1;
    }

    bzero(&newtio, sizeof(newtio));

    //c_cflag标志可以定义CLOCAL和CREAD，这将确保该程序不被其他端口控制和信号干扰，同时串口驱动将读取进入的数据。CLOCAL和CREAD通常总是被是能的
    newtio.c_cflag |= CLOCAL | CREAD;

    switch (nBits) {//设置数据位数
        case 5:
            newtio.c_cflag &= ~CSIZE;
            newtio.c_cflag |= CS5;
            break;
        case 6:
            newtio.c_cflag &= ~CSIZE;
            newtio.c_cflag |= CS6;
            break;
        case 7:
            newtio.c_cflag &= ~CSIZE;
            newtio.c_cflag |= CS7;
            break;
        case 8:
            newtio.c_cflag &= ~CSIZE;
            newtio.c_cflag |= CS8;
            break;
        default:
            break;
    }
    LOGW("nBits:%d,invalid param", nBits);

    switch (nEvent) {//设置校验位
        case 'O':
            newtio.c_cflag |= (PARODD | PARENB); /* 设置为奇效验*/
            newtio.c_iflag |= INPCK;
            break;
        case 'E':
            newtio.c_cflag |= PARENB; /* Enable parity */
            newtio.c_cflag &= ~PARODD; /* 转换为偶效验*/
            newtio.c_iflag |= INPCK;
            break;
        case 'N':
            newtio.c_cflag &= ~PARENB;//清除校验位
            newtio.c_iflag &= ~INPCK;//Enable parity checking
            break;
        case 'S':
            newtio.c_cflag &= ~PARENB;
            newtio.c_cflag &= ~CSTOPB;
            newtio.c_iflag |= INPCK;
            break;
        default:
            break;
    }
    LOGW("nEvent:%c,invalid param", nEvent);

    if (getBaudrate(nSpeed) != -1) {
        cfsetispeed(&newtio, getBaudrate(nSpeed));
        cfsetospeed(&newtio, getBaudrate(nSpeed));
        LOGW("nSpeed:%d,invalid param", nSpeed);
    }

    switch (nStop) {//设置停止位
        case 1:
            newtio.c_cflag &= ~CSTOPB;
            break;
        case 2:
            newtio.c_cflag |= CSTOPB;
            break;
        default:
            break;
    }
    LOGW("nStop:%d,invalid param", nStop);

    tcflush(mTtyfd, TCIFLUSH);
    newtio.c_cc[VTIME] = 0;//设置等待时间
    newtio.c_cc[VMIN] = 0;//设置最小接收字符

    if (tcsetattr(mTtyfd, TCSANOW, &newtio) != 0) {
        LOGE("options set error");
        return -1;
    }

    LOGW("config set success");

    return 1;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_black_serialport_SerialPort_close
        (JNIEnv *env, jobject thiz) {
    jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
    jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

    jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

    jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
    jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

    LOGD("close(fd = %d)", descriptor);
    close(descriptor);
}

