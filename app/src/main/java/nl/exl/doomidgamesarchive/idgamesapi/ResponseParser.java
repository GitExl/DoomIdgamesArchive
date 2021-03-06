package nl.exl.doomidgamesarchive.idgamesapi;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Parses XML from an input stream into an Idgames mResponse object.
 */
class ResponseParser {

    // The XML mReader object.
    private XMLReader mReader;
    
    // The mHandler for SAX responses.
    private ResponseHandler mHandler; 

    ResponseParser() {
        try {
            // Instantiate the parsers and readers.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            mReader = parser.getXMLReader();
        } catch (SAXException e) {
            Log.w("ResponseParser", "Cannot initialise SAX parser: " + e.toString());
        } catch (ParserConfigurationException e) {
            Log.w("ResponseParser", "Cannot configure SAX parser: " + e.toString());
        }
        
        mHandler = new ResponseHandler();
        mReader.setContentHandler(mHandler);
    }
    
    /**
     * Attempts to parse the InputStream object.
     * 
     * @param input The XML input to parse.
     */
    void parse(InputStream input) {
        try {
            mReader.parse(new InputSource(input));
        } catch (SAXException e) {
            Log.w("ResponseParser", "Cannot parse XML, SAXException: " + e.toString());
        } catch (IOException e) {
            Log.w("ResponseParser", "Cannot parse XML, IOException: " + e.toString());
        }
    }
    
    void setContainsSingleFile() {
        mHandler.setContainsSingleFile();
    }
    
    Response getResponse() {
        return mHandler.getResponse();
    }
    
    /**
     * SAX mResponse mHandler for Idgames web API XML responses.
     */
    private static class ResponseHandler extends DefaultHandler {
        // Parsing states.
        private static final int STATE_UNKNOWN = -1;
        private static final int STATE_CONTENT = 0;
        private static final int STATE_ERROR = 1;
        private static final int STATE_WARNING = 2;
        private static final int STATE_FILE = 3;
        private static final int STATE_DIRECTORY = 4;
        private static final int STATE_VOTE = 5;
        private static final int STATE_REVIEW = 6;
        
        // The current parsing mState.
        private int mState = STATE_UNKNOWN;
        
        // The current mElement (tag) name.
        private String mElement;
        
        // The contents of the current text file entry.
        private StringBuilder mTextFileContents;
        
        // Entries and related data currently being parsed, waiting to be inserted into the
        // mResponse object.
        private FileEntry mFileEntry;
        private DirectoryEntry mDirectoryEntry;
        private VoteEntry mVoteEntry;
        private Review mReview;

        // The mResponse object being constructed.
        private Response mResponse = null;
        
        // If true, indicates that the XML file contains a single Idgames file's information
        // instead of multiple entries' information.
        private boolean mContainsSingleFile = false;
        
        
        @Override
        public void startDocument() {
            mResponse = new Response();
        }
        
        Response getResponse() {
            return mResponse;
        }
        
        void setContainsSingleFile() {
            mContainsSingleFile = true;
        }
        
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            if (localName.equals("idgames-response")) {
                mResponse.setVersion(Float.parseFloat(atts.getValue("version")));
            
            // Messages.
            } else if (localName.equals("error")) {
                mState = STATE_ERROR;
            } else if (localName.equals("warning")) {
                mState = STATE_WARNING;
                
            // Content.
            } else if (localName.equals("content")) {
                if (mContainsSingleFile) {
                    mState = STATE_FILE;    
                    mFileEntry = new FileEntry();
                    mTextFileContents = new StringBuilder();
                } else {
                    mState = STATE_CONTENT;
                }
            
            // Content sub-tags.
            } else if (mState == STATE_CONTENT) {
                if (localName.equals("file")) {
                    mState = STATE_FILE;
                    mFileEntry = new FileEntry();
                    mTextFileContents = new StringBuilder();
                } else if (localName.equals("vote")) {
                    mState = STATE_VOTE;
                    mVoteEntry = new VoteEntry();
                } else if (localName.equals("dir")) {
                    mState = STATE_DIRECTORY;
                    mDirectoryEntry = new DirectoryEntry();
                }
            
            // File.
            } else if (mState == STATE_FILE) {
                if (localName.equals("review")) {
                    mState = STATE_REVIEW;
                    mReview = new Review();
                }
            }
                
            mElement = localName;
        }
        
        @Override
        public void endElement(String namespaceURI, String localName, String qName) {
            // Messages.
            if (mState == STATE_ERROR && localName.equals("error")) {
                mState = STATE_UNKNOWN;
            } else if (mState == STATE_WARNING && localName.equals("warning")) {
                mState = STATE_UNKNOWN;
            
            // Content.
            } else if (mState == STATE_CONTENT && localName.equals("content")) {
                mState = STATE_UNKNOWN;
            } else if (mContainsSingleFile && mState == STATE_FILE && localName.equals("content")) {
                mState = STATE_UNKNOWN;
                
                mFileEntry.addTextFileContents(mTextFileContents.toString());
                mResponse.addEntry(mFileEntry);
                mFileEntry = null;
                mTextFileContents = null;

            // File.
            } else if (mState == STATE_FILE && localName.equals("file")) {
                mState = STATE_CONTENT;
                
                mFileEntry.addTextFileContents(mTextFileContents.toString());
                mResponse.addEntry(mFileEntry);
                mFileEntry = null;
                mTextFileContents = null;
                
            // Vote.
            } else if (mState == STATE_VOTE && localName.equals("vote")) {
                mState = STATE_CONTENT;
                
                mResponse.addEntry(mVoteEntry);
                mVoteEntry = null;
            
            // Directory.
            } else if (mState == STATE_DIRECTORY && localName.equals("dir")) {
                mState = STATE_CONTENT;
                mResponse.addEntry(mDirectoryEntry);
                mDirectoryEntry = null;
            
            // Review.  
            } else if (mState == STATE_REVIEW && localName.equals("review")) {
                mState = STATE_FILE;
                
                mFileEntry.addReview(mReview);
                mReview = null;
                
            }
            
            mElement = null;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) {
            if (mElement == null)
                return;
            
            String chars = new String(ch, start, length);
            
            // File or content file (single file) entry.
            if (mState == STATE_FILE) {
                if (mElement.equals("id")) {
                    mFileEntry.setId(Integer.parseInt(chars));
                } else if (mElement.equals("title")) {
                    mFileEntry.addTitle(chars);
                } else if (mElement.equals("dir")) {
                    mFileEntry.addFilePath(chars);
                } else if (mElement.equals("filename")) {
                    mFileEntry.addFileName(chars);
                } else if (mElement.equals("size")) {
                    mFileEntry.setFileSize(Integer.parseInt(chars));
                } else if (mElement.equals("date")) {
                    mFileEntry.addDate(chars);
                } else if (mElement.equals("author")) {
                    mFileEntry.addAuthor(chars);
                } else if (mElement.equals("email")) {
                    mFileEntry.addEmail(chars);
                } else if (mElement.equals("description")) {
                    mFileEntry.addDescription(chars);
                } else if (mElement.equals("rating")) {
                    mFileEntry.setRating(Double.parseDouble(chars));
                } else if (mElement.equals("votes")) {
                    mFileEntry.setVoteCount(Integer.parseInt(chars));

                // Single file tags only.
                } else if (mElement.equals("credits")) {
                    mFileEntry.addCredits(chars);
                } else if (mElement.equals("base")) {
                    mFileEntry.addBase(chars);
                } else if (mElement.equals("buildtime")) {
                    mFileEntry.addBuildTime(chars);
                } else if (mElement.equals("editors")) {
                    mFileEntry.addEditorsUsed(chars);
                } else if (mElement.equals("bugs")) {
                    mFileEntry.addBugs(chars);
                } else if (mElement.equals("textfile")) {
                    mTextFileContents.append(chars);
                }
            }

            // Review.
            if (mState == STATE_REVIEW) {
                if (mElement.equals("text")) {
                    mReview.addText(chars);
                } else if (mElement.equals("vote")) {
                	mReview.setRating(Float.parseFloat(chars));
                } else if (mElement.equals("username")) {
                    mReview.setUsername(chars);
                }
                
            // Vote.
            } else if (mState == STATE_VOTE) {
                if (mElement.equals("id")) {
                    mVoteEntry.setId(Integer.parseInt(chars));
                } else if (mElement.equals("file")) {
                    mVoteEntry.setFileId(Integer.parseInt(chars));
                } else if (mElement.equals("reviewtext")) {
                    mVoteEntry.addReviewText(chars);
                } else if (mElement.equals("title")) {
                    mVoteEntry.addTitle(chars);
                } else if (mElement.equals("rating")) {
                    mVoteEntry.setRating(Double.parseDouble(chars));
                } else if (mElement.equals("author")) {
                    mVoteEntry.setAuthor(chars);
                }
                
            // Directory.
            } else if (mState == STATE_DIRECTORY) {
                if (mElement.equals("id")) {
                    mDirectoryEntry.setId(Integer.parseInt(chars));
                } else if (mElement.equals("name")) {
                    mDirectoryEntry.addName(chars);
                }

            // Error.
            } else if (mState == STATE_ERROR) {
                if (mElement.equals("warning")) {
                    mResponse.setErrorMessage(chars);
                }
                
            // Warning.
            } else if (mState == STATE_WARNING) {
                if (mElement.equals("type")) {
                    mResponse.setWarningType(chars);
                }
            }
        }
    }
}
