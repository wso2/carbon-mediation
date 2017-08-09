/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediation.connector.compression;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;

/**
 * This is the Gzip input stream wrapper, which will take normal input stream, compress it and provide gzip compressed
 * input stream.
 */
public class GZipCompressInputStream extends DeflaterInputStream {

    // Current processing block
    private Block block;

    // Processing position
    private int position = 0;

    // Gzip tail
    private byte[] gZipTail = new byte[8];

    // GZIP header magic number
    private final static int GZIP_MAGIC = 0x8b1f;

    // The GZIP header
    private final static byte[] GZIP_HEADER = {
            (byte) GZIP_MAGIC, // Magic number (short).
            (byte) (GZIP_MAGIC >> 8), // Magic number (short).
            Deflater.DEFLATED, // Compression method (CM).
            0, // Flags (FLG).
            0, // Modification time MTIME (int).
            0, // Modification time MTIME (int).
            0, // Modification time MTIME (int).
            0, // Modification time MTIME (int).
            0, // Extra flags (XFLG).
            0 // Operating system (OS).
    };

    /**
     * Constructor which wraps given input stream.
     *
     * @param in {@link InputStream} object
     */
    public GZipCompressInputStream(InputStream in) {
        super(new CheckedInputStream(in, new CRC32()), new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        block = Block.HEAD;
    }

    /**
     * Overridden read method which will inject proper gzip header and gzip tail.
     *
     * @param b buffer to read into.
     * @param off offset
     * @param len length
     * @return cnt number of bytes read.
     * @throws IOException
     */
    public int read(byte b[], int off, int len)  throws IOException {
        int cnt = 0;
        switch (block) {
            case HEAD:
                cnt = Math.min(len, GZIP_HEADER.length - position);
                System.arraycopy(GZIP_HEADER, position, b, off, cnt);
                //Increment the position.
                position += cnt;
                if (position == GZIP_HEADER.length) {
                    //Complete header is read, moving to body.
                    block = Block.BODY;
                }
            case BODY:
                if (cnt < len) {
                    //If buffer is not filled, read body as well.
                    int i = len - cnt;
                    int r = super.read(b, off + cnt, i);
                    if (r < i) {
                        // Nothing read from body part, moving to trailer
                        createTail();
                        block = Block.TAIL;
                        position = 0;
                    }
                    if (r >= 0) {
                        cnt += r; // increase counter of read bytes
                    }

                }
            case TAIL:
                if (cnt < len) {
                    int i = Math.min(len - cnt, gZipTail.length - position);
                    if (i > 0) {
                        System.arraycopy(gZipTail, position, b, off + cnt, i);
                        // Advance the position as "count" bytes have already been read.
                        position += i;
                        // And also increase counter of number of bytes read
                        cnt += i;
                    }
                }
        }
        return (cnt > 0 ? cnt : -1);
    }

    /**
     * Create the GZIP tail for the currently read and compressed data
     *
     * @throws IOException If an I/O error is produced.
     */
    private void createTail() throws IOException {
        writeInt((int) ((CheckedInputStream) this.in).getChecksum().getValue(), gZipTail, 0); // CRC-32 of uncompr. data
        writeInt(def.getTotalIn(), gZipTail, 4); // Number of uncompr. bytes
    }

    /**
     * Writes an integer in Intel byte order to a byte array, starting at a given offset.
     *
     * @param i         The integer to write.
     * @param buf       The byte array to write the integer to.
     * @param offset    The offset from which to start writing.
     * @throws IOException If an I/O error is produced.
     */
    private void writeInt(int i, byte[] buf, int offset) throws IOException {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /**
     * Writes a short integer in Intel byte order to a byte array, starting at a given offset.
     *
     * @param s         The short to write.
     * @param buf       The byte array to write the integer to.
     * @param offset    The offset from which to start writing.
     * @throws IOException If an I/O error is produced.
     */
    private void writeShort(int s, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte) (s & 0xff);
        buf[offset + 1] = (byte) ((s >> 8) & 0xff);
    }

    enum Block {
        HEAD, BODY, TAIL
    }
}
