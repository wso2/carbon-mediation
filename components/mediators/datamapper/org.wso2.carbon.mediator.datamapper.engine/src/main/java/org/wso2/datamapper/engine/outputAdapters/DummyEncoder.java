/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.datamapper.engine.outputAdapters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.avro.io.Encoder;
import org.apache.avro.util.Utf8;

public class DummyEncoder extends Encoder {
	
	OutputStream outputStream ;
	
	public DummyEncoder(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void flush() throws IOException {
		outputStream.flush();

	}

	@Override
	public void setItemCount(long arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startItem() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeArrayEnd() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeArrayStart() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeBoolean(boolean arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeBytes(ByteBuffer arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeBytes(byte[] arg0, int arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeDouble(double arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeEnum(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeFixed(byte[] arg0, int arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeFloat(float arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeIndex(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeInt(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeLong(long arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeMapEnd() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeMapStart() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeNull() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeString(Utf8 data) throws IOException {
		outputStream.write(data.toString().getBytes());
	}

}
