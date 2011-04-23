
#


CFLAGS=-I.. -I/System/Library/Frameworks/JavaVM.framework/Headers 
OBJS=HIDManager.o HIDDeviceInfo.o HIDDevice.o
EXTRAOBJS=../mac/hid.o
SHAREDLIB=hdiapi-jni.dylib
SHAREDLIBVER=1.0
LIBS=-framework IOKit -framework CoreFoundation -l iconv

all: $(SHAREDLIB)

$(SHAREDLIB): $(OBJS)
	gcc -dynamiclib -current_version $(SHAREDLIBVER) $(OBJS) $(EXTRAOBJS) $(LIBS) -o $(SHAREDLIB)

clean:
	rm -f $(OBJS) $(SHAREDLIB)