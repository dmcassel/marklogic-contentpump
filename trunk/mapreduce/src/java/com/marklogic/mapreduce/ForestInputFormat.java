package com.marklogic.mapreduce;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.marklogic.io.BiendianDataInputStream;
import com.marklogic.mapreduce.utilities.InternalUtilities;

public class ForestInputFormat<VALUE> extends
        FileInputFormat<DocumentURI, VALUE> implements MarkLogicConstants {
    public static final Log LOG = LogFactory.getLog(ForestInputFormat.class);
    static final int STREAM_BUFFER_SIZE = 1 << 24;

    @Override
    public RecordReader<DocumentURI, VALUE> createRecordReader(
            InputSplit split, TaskAttemptContext context) throws IOException,
            InterruptedException {
        return new ForestReader<VALUE>();
    }

    protected List<FileStatus> listStatus(JobContext job) throws IOException {
        List<FileStatus> result = super.listStatus(job);
        for (Iterator<FileStatus> it = result.iterator(); it.hasNext();) {
            FileStatus file = it.next();
            String fileName = file.getPath().getName();
            if (!file.isDir() || fileName.equals("Journals")
                    || fileName.equals("Large")) {
                it.remove();
            }
        }
        return result;
    }

    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException {
        long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
        long maxSize = getMaxSplitSize(job);
        
        // generate splits
        List<InputSplit> splits = new ArrayList<InputSplit>();
        List<FileStatus> files = listStatus(job);
        for (FileStatus file : files) { // stand directories
            Path path = file.getPath();
            FileSystem fs = path.getFileSystem(job.getConfiguration());
            FileStatus children[] = fs.listStatus(path);
            FileStatus treeIndexStatus = null, treeDataStatus = null;
            for (FileStatus child : children) {
                String fileName = child.getPath().getName();
                if (fileName.equals("TreeData")) { // inside a stand
                    treeDataStatus = child;
                } else if (fileName.equals("TreeIndex")) {
                    treeIndexStatus = child;
                }
                if (treeDataStatus != null && treeIndexStatus != null) {
                    break;
                }
            }
            if (treeDataStatus == null) {
                throw new RuntimeException("TreeData file not found.");
            } else if (treeIndexStatus == null) {
                throw new RuntimeException("TreeIndex file not found.");
            }
            long treeDataSize = treeDataStatus.getLen();
            if (treeDataSize == 0) {
                // unexpected, give up this stand
                LOG.warn("Found empty TreeData file.  Skipping...");
                break;
            }
            Path treeDataPath = treeDataStatus.getPath();
            long blockSize = treeDataStatus.getBlockSize();
            long splitSize = computeSplitSize(blockSize, minSize, maxSize);
            // make splits based on TreeIndex
            FSDataInputStream is = fs.open(treeIndexStatus.getPath());
            BiendianDataInputStream in = new BiendianDataInputStream(is);
            int prevDocid = -1, docid, position = 0;
            long prevOffset = -1L, offset = 0, splitStart = 0;
            BlockLocation[] blkLocations = fs.getFileBlockLocations(
                    treeDataStatus, 0, treeDataSize);
            for (;; ++position) {
                try {
                    docid = in.readInt();
                    in.readInt();
                    offset = in.readLong();
                } catch (EOFException e) {
                    break;
                }
                int comp = InternalUtilities.compareUnsignedLong(offset, 
                        treeDataSize);
                if (comp >= 0) {
                    throw new RuntimeException(
                        "TreeIndex offset is out of bound: position = " +
                        position + ", offset = " + offset + ", treeDataSize = "
                        + treeDataSize);
                }
                if (prevDocid != -1 && 
                    (docid & 0xffffffffL) <= (prevDocid & 0xffffffffL)) {
                    throw new RuntimeException(
                        "docid out of order, position = " +
                        position + ", docid = " + docid + ", prevDocid = " +
                        prevDocid);
                }
                prevDocid = docid;
                if (prevOffset != -1L &&
                    InternalUtilities.compareUnsignedLong(offset, prevOffset) 
                        <= 0) {
                    throw new RuntimeException(
                        "offset out of order, position = " +
                        position + ", offset = " + offset + ", prevOffset = "
                        + prevOffset);
                }
                long splitLen = offset - splitStart;
                if (splitLen == splitSize ||
                    (splitLen > splitSize && 
                     splitLen - splitSize <= 
                         splitSize - (prevOffset - splitStart))) { 
                    int blkIndex = getBlockIndex(blkLocations, offset);
                    FileSplit split = new FileSplit(treeDataPath, splitStart, 
                            splitSize, blkLocations[blkIndex].getHosts());
                    splits.add(split);
                    splitStart = offset;
                } else if (splitLen > splitSize) {
                    int blkIndex = getBlockIndex(blkLocations, prevOffset);
                    FileSplit split = new FileSplit(treeDataPath, splitStart,
                            prevOffset - splitStart, 
                            blkLocations[blkIndex].getHosts());
                    splits.add(split);
                    splitStart = prevOffset;
                }              
            }
            if (offset > splitStart) {
                int blkIndex = getBlockIndex(blkLocations, offset);
                FileSplit split = new FileSplit(treeDataPath, splitStart, 
                        splitSize, blkLocations[blkIndex].getHosts());
                splits.add(split);
            }
        }
        return splits;
    }
}
