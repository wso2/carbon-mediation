CC=g++
CFLAGS=-c -g -Wall -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H
THRIFT_INCLUDE_DIR := /usr/local/include/thrift
THRIFT_LIB_DIR=/usr/local/lib


all: CGAgent 

CGAgent: CGAgent.o CloudGatewayService.o cg_constants.o cg_types.o 
	$(CC) gen/CGAgent.o -L$(THRIFT_LIB_DIR) gen/CloudGatewayService.o gen/cg_constants.o gen/cg_types.o -g -o CGAgent -lthrift

CGAgent.o: CGAgent.cpp
	$(CC) $(CFLAGS) -I$(THRIFT_INCLUDE_DIR) CGAgent.cpp -o gen/CGAgent.o

CloudGatewayService.o: gen/CloudGatewayService.cpp 
	$(CC) $(CFLAGS) -I$(THRIFT_INCLUDE_DIR)  gen/CloudGatewayService.cpp -o gen/CloudGatewayService.o

cg_constants.o: gen/cg_constants.cpp 
	$(CC) $(CFLAGS) -I$(THRIFT_INCLUDE_DIR) gen/cg_constants.cpp -o gen/cg_constants.o

cg_types.o: gen/cg_types.cpp
	$(CC) $(CFLAGS) -I$(THRIFT_INCLUDE_DIR) gen/cg_types.cpp -o gen/cg_types.o

clean:
	rm -rf *.o gen/*.o CGAgent 
