/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_black_serialport_SerialPort */

#ifndef _Included_com_black_serialport_SerialPort
#define _Included_com_black_serialport_SerialPort
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_black_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_com_black_serialport_SerialPort_open
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_black_serialport_SerialPort
 * Method:    setConfig
 * Signature: (ICII)I
 */
JNIEXPORT jint JNICALL Java_com_black_serialport_SerialPort_setConfig
  (JNIEnv *, jobject, jint, jchar, jint, jint);

/*
 * Class:     com_black_serialport_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_black_serialport_SerialPort_close
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif