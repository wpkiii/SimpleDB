package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private File file;
    private TupleDesc tupleDesc;
    private int id;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.file = f;
        this.tupleDesc = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.tupleDesc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        int offset = pid.getPageNumber() * BufferPool.getPageSize();
        byte[] data = new byte[BufferPool.getPageSize()];

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            raf.readFully(data);
            return new HeapPage(new HeapPageId(getId(), pid.getPageNumber()), data);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading the page from file.", e);
        }
//        try {
//            RandomAccessFile f = new RandomAccessFile(this.file,"r");
//            int offset = BufferPool.getPageSize() * pid.getPageNumber();
//            byte[] data = new byte[BufferPool.getPageSize()];
//            if (offset + BufferPool.getPageSize() > f.length()) {
//                System.err.println("Page offset exceeds max size, error!");
//                System.exit(1);
//            }
//            f.seek(offset);
//            f.readFully(data);
//            f.close();
//            return new HeapPage((HeapPageId) pid, data);
//        } catch (FileNotFoundException e) {
//            System.err.println("FileNotFoundException: " + e.getMessage());
//            throw new IllegalArgumentException();
//        } catch (IOException e) {
//            System.err.println("Caught IOException: " + e.getMessage());
//            throw new IllegalArgumentException();
//        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int) (Math.ceil((double) file.length() / BufferPool.getPageSize()));
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    public DbFileIterator iterator(TransactionId tid) {
        return new HeapFileIterator(this, tid);
    }

    /**
     * Helper class implementing the Java Iterator for tuples on a HeapFile.
     */
    class HeapFileIterator extends AbstractDbFileIterator {

        private Iterator<Tuple> tupleIterator;
        private int currentPageNumber;
        private TransactionId tid;
        private HeapFile heapFile;

        /**
         * Constructs an iterator for a HeapFile.
         *
         * @param heapFile the HeapFile to iterate over.
         * @param tid the transaction ID associated with the iterator.
         */
        public HeapFileIterator(HeapFile heapFile, TransactionId tid) {
            this.heapFile = heapFile;
            this.tid = tid;
        }

        public void open() throws DbException, TransactionAbortedException {
            this.currentPageNumber = -1;
        }

        protected Tuple readNext() throws TransactionAbortedException, DbException {
            // If there are no more tuples in the current iterator, nullify it.
            if (tupleIterator != null && !tupleIterator.hasNext()) {
                tupleIterator = null;
            }
            while (tupleIterator == null && currentPageNumber < heapFile.numPages() - 1) {
                currentPageNumber++;
                HeapPageId currentPageId = new HeapPageId(heapFile.getId(), currentPageNumber);
                HeapPage currentPage = (HeapPage) Database.getBufferPool().getPage(tid, currentPageId, Permissions.READ_ONLY);

                tupleIterator = currentPage.iterator();
                if (!tupleIterator.hasNext()) {
                    tupleIterator = null;
                }
            }
            return tupleIterator == null ? null : tupleIterator.next();
        }
        public void rewind() throws DbException, TransactionAbortedException {
            close();
            open();
        }

        public void close() {
            super.close();
            tupleIterator = null;
            currentPageNumber = Integer.MAX_VALUE;
        }
    }

}

