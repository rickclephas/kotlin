.PHONY: all

all: exe libso1.so

exe: exe.c
	gcc exe.c -o exe -ldl -lpthread -Wl,-rpath,.

libso1.so: libso2.so so1.c
	gcc so1.c -shared -fPIC -lso2 -L. -o libso1.so

libso2.so: so2.c
	gcc so2.c -shared -fPIC -o libso2.so

clean:
	rm -f exe *.so
