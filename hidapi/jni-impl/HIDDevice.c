#include <stdio.h>
#include <assert.h>

#include <jni-stubs/com_codeminders_hidapi_HIDDevice.h>
#include "hid-java.h"

static long getPeer(JNIEnv *env, jobject self)
{
    jclass cls = (*env)->FindClass(env, DEV_CLASS);
    assert(cls!=NULL);
    if (cls == NULL) 
        return 0;
    jfieldID fid = (*env)->GetFieldID(env, cls, "peer", "J");
    return (*env)->GetLongField(env, self, fid); 
}

static void setPeer(JNIEnv *env, jobject self, long peer)
{
    jclass cls = (*env)->FindClass(env, DEV_CLASS);
    assert(cls!=NULL);
    if (cls == NULL) 
        return; //TODO: error handling
    jfieldID fid = (*env)->GetFieldID(env, cls, "peer", "J");
    (*env)->SetLongField(env, self, fid, peer);     
}

JNIEXPORT void JNICALL Java_com_codeminders_hidapi_HIDDevice_close
  (JNIEnv *env, jobject self)
{
    long peer = getPeer(env, self);
    if (peer == 0) 
        return; /* not an error, freed previously */ 
    hid_close(peer);
    setPeer(env, self, 0);
}

JNIEXPORT jint JNICALL Java_com_codeminders_hidapi_HIDDevice_write
  (JNIEnv *env, jobject obj, jbyteArray data)
{
}

JNIEXPORT jint JNICALL Java_com_codeminders_hidapi_HIDDevice_read
  (JNIEnv *env, jobject obj, jbyteArray data)
{
}

JNIEXPORT void JNICALL Java_com_codeminders_hidapi_HIDDevice_enableBlocking
  (JNIEnv *env, jobject obj)
{
}

JNIEXPORT void JNICALL Java_com_codeminders_hidapi_HIDDevice_disableBlocking
  (JNIEnv *env, jobject obj)
{
}

JNIEXPORT jint JNICALL Java_com_codeminders_hidapi_HIDDevice_sendFeatureReport
  (JNIEnv *env, jobject obj, jbyteArray data)
{
}

JNIEXPORT jint JNICALL Java_com_codeminders_hidapi_HIDDevice_getFeatureReport
  (JNIEnv *env, jobject obj, jbyteArray data)
{
}

JNIEXPORT jstring JNICALL Java_com_codeminders_hidapi_HIDDevice_getManufacturerString
  (JNIEnv *env, jobject obj)
{
}

JNIEXPORT jstring JNICALL Java_com_codeminders_hidapi_HIDDevice_getProductString
  (JNIEnv *env, jobject obj)
{
}

JNIEXPORT jstring JNICALL Java_com_codeminders_hidapi_HIDDevice_getSerialNumberString
  (JNIEnv *env, jobject obj)
{
}
JNIEXPORT jstring JNICALL Java_com_codeminders_hidapi_HIDDevice_getIndexedString
  (JNIEnv *env, jobject obj, jint ind)
{
}

JNIEXPORT jstring JNICALL Java_com_codeminders_hidapi_HIDDevice_getLastError
  (JNIEnv *env, jobject obj)
{
}
