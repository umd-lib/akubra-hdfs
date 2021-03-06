/*
   Copyright 2011 FIZ Karlsruhe 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package de.fiz.akubra.hdfs;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Random;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class HDFSBlobStoreConnectionTest {

    private HDFSBlobStore mockStore;
    private FileSystem mockFs;
    private URI mockId = URI.create("hdfs://localhost:9000/");

    private FileStatus[] createTestFileStatus() {
        FileStatus[] states = new FileStatus[] { new FileStatus(1024, false, 0, 0, 0, new Path("hdfs://test1")),
                new FileStatus(2024, false, 0, 0, 0, new Path("hdfs://test2")), new FileStatus(3024, false, 0, 0, 0, new Path("hdfs://test3")),
                new FileStatus(4024, false, 0, 0, 0, new Path("hdfs://test4")), new FileStatus(5024, false, 0, 0, 0, new Path("hdfs://test5")) };
        return states;
    }

    @Before
    public void setUp() throws Exception {
        mockStore = createMock(HDFSBlobStore.class);
        mockFs = createMock(FileSystem.class);
    }

    @Test
    public void testClose() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId);
        mockFs.close();
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        connection.close();
    }

    private HDFSBlobStoreConnection createTestConnection() throws Exception {
        HDFSBlobStoreConnection connection = new HDFSBlobStoreConnection(mockStore);
        Field f = HDFSBlobStoreConnection.class.getDeclaredField("hdfs");
        f.setAccessible(true);
        f.set(connection, mockFs);
        return connection;
    }

    @Test
    public void testCreateBlob1() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId).times(3);
        expect(mockFs.exists((Path) anyObject())).andReturn(false);
        expect(mockFs.create((Path) anyObject())).andReturn(new FSDataOutputStream(new ByteArrayOutputStream(20), null));
        expect(mockFs.exists((Path) anyObject())).andReturn(true);
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        byte[] buf = new byte[4096];
        new Random().nextBytes(buf);
        HDFSBlob b = (HDFSBlob) connection.getBlob(new ByteArrayInputStream(buf), 4096, null);
        assertNotNull(b);
        assertTrue(b.getConnection() == connection);
        assertTrue(b.exists());
    }

    @Test
    public void testGetBlob1() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId).times(3);
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        HDFSBlob b = (HDFSBlob) connection.getBlob(new URI("hdfs://localhost:9000/test"), null);
        assertNotNull(b);
        assertTrue(b.getConnection() == connection);
    }

    @Test
    public void testGetBlobStore() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId);
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        assertNotNull(connection.getBlobStore());
        assertTrue(connection.getBlobStore() == mockStore);
        assertTrue(connection.getBlobStore() instanceof HDFSBlobStore);
    }

    @Test
    public void testGetFileSystem() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId);
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        assertNotNull(connection.getFileSystem());
        assertTrue(connection.getFileSystem() == mockFs);
    }

    @Test
    public void testHDFSBlobStoreConnection() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId);
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        assertNotNull(connection);
    }

    @Test
    public void testListBlobIds() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockFs.listStatus((Path) anyObject())).andReturn(createTestFileStatus()).times(2);
        expect(mockStore.getId()).andReturn(URI.create("hdfs://localhost:9000/")).times(2);
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        HDFSIdIterator it = (HDFSIdIterator) connection.listBlobIds("/");
        assertNotNull(it);
    }

    @Test
    public void testReopen() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId);
        mockFs.close();
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        assertFalse(connection.isClosed());
        connection.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSync() throws Exception {
        expect(mockStore.getFilesystem()).andReturn(mockFs);
        expect(mockStore.getId()).andReturn(mockId);
        replay(mockStore, mockFs);
        HDFSBlobStoreConnection connection = createTestConnection();
        connection.sync();
    }
}
