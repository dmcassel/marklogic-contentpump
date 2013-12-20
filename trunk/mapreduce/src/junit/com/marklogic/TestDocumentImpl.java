package com.marklogic;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import com.marklogic.dom.DocumentImpl;
import com.marklogic.dom.ElementImpl;
import com.marklogic.tree.ExpandedTree;

public class TestDocumentImpl extends TestCase {
    boolean verbose = false;
    String testData = "src/testdata";
    String forest = "3docForest";
    String stand = "00000002";
    int num = 3;


    public void testGetDocumentURI() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
                + forest, stand), false);
        assertEquals(num, trees.size());

        StringBuffer expected = new StringBuffer();
        StringBuffer actual = new StringBuffer();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
            expected.append(t.getDocumentURI());
            DocumentImpl d = new DocumentImpl(t, i);
            actual.append(d.getDocumentURI());
        }
        assertEquals(expected.toString(), actual.toString());
    }
    
    public void testGetNodeNameAndFirstAndLastChild() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuffer expected = new StringBuffer();
        StringBuffer actual = new StringBuffer();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
        	String uri = t.getDocumentURI();
        	
        	Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            expected.append(doc.getNodeName());
            expected.append("#FIRSTCHILD##").
            		 append(doc.getFirstChild().getNodeName()).append("#");
            expected.append("#LASTCHILD##").
            		 append(doc.getLastChild().getNodeName()).append("#");
            
            DocumentImpl d = new DocumentImpl(t, 0);
            actual.append(d.getNodeName());
            String lname = d.getFirstChild().getNodeName();
            actual.append("#FIRSTCHILD##").append(lname).append("#");
            lname = d.getLastChild().getNodeName();
            actual.append("#LASTCHILD##").append(lname).append("#");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
    public void testGetOwnerDocumentBaseURI() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuffer expected = new StringBuffer();
        StringBuffer actual = new StringBuffer();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
        	String uri = t.getDocumentURI();
        	
        	Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            expected.append(doc.getNodeName());
            expected.append("#OWNDERDOCROOT##").
            		 append(doc.getOwnerDocument() == null).append("#");
            expected.append("#OWNDERDOCCHILD##").
            		 append(doc.getFirstChild().getOwnerDocument().getNodeName()).append("#");
            
            DocumentImpl d = new DocumentImpl(t, 0);
            actual.append(d.getNodeName());
            actual.append("#OWNDERDOCROOT##").
   		 		   append(d.getOwnerDocument() == null).append("#");
            actual.append("#OWNDERDOCCHILD##").
   		 		   append(d.getFirstChild().getOwnerDocument().getNodeName()).append("#");
            
            String expectedUri = doc.getBaseURI();
            String actualUri = d.getBaseURI();
            
            if (!expectedUri.contains(actualUri)) {
                expected.append("#BASEURIROOT##").
       		 			 append(expectedUri).append("#");
                actual.append("#BASEURIROOT##").
		 			 append(actualUri).append("#");
            }
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
    public void testGetLocalNameGetNamespaceURI() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuffer expected = new StringBuffer();
        StringBuffer actual = new StringBuffer();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
        	String uri = t.getDocumentURI();
        	
        	Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            DocumentImpl d = new DocumentImpl(t, 0);
        	
            expected.append(d.getBaseURI()).append("\n");
            actual.append(d.getBaseURI()).append("\n");
            
            NodeList children = doc.getFirstChild().getChildNodes();
            for (int k = 0; k < children.getLength(); k++) {
              Node curr = children.item(k);
              expected.append("#LOCALNAME##").append(curr.getLocalName()).append("#").append("\n");
              expected.append("#URI##").append(curr.getNamespaceURI()).append("#").append("\n");
              expected.append("\n");
            }
            children = d.getFirstChild().getChildNodes();
            for (int k = 0; k < children.getLength(); k++) {
              Node curr = children.item(k);
              actual.append("#LOCALNAME##").append(curr.getLocalName()).append("#").append("\n");
              actual.append("#URI##").append(curr.getNamespaceURI()).append("#").append("\n");
              actual.append("\n");
            }
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
    public void testGetElementByTagName() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuffer expected = new StringBuffer();
        StringBuffer actual = new StringBuffer();
        
        String tags[] = {"*","country","capital"};
        
        for (int s = 0; s < tags.length; s++){
        	expected.append("#TAG#").append(tags[s]).append("\n");
        	actual.append("#TAG#").append(tags[s]).append("\n");
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
        	String uri = t.getDocumentURI();
        	
        	expected.append("#URI##").append(uri).append("#\n");
        	actual.append("#URI##").append(uri).append("#\n");
        	
        	Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
        	Element rootExpected = (Element) doc.getFirstChild();
        	NodeList elementsExpected = rootExpected.getElementsByTagName(tags[s]);
        	for (int j = 0; j < elementsExpected.getLength(); j++) {
        		Node curr = elementsExpected.item(j);
        		expected.append("#I##").append(j).append("#").
        				append("#NODENAME##").append(curr.getNodeName()).append("#").
        				append("#NODETEXT##").append(curr.getTextContent()).append("#").
        				append("\n");
        	}
        		            
            DocumentImpl d = new DocumentImpl(t, 0);
            ElementImpl rootActual = (ElementImpl) d.getFirstChild();
        	NodeList elementsActual = rootActual.getElementsByTagName(tags[s]);
        	for (int j = 0; j < elementsActual.getLength(); j++) {
        		Node curr = elementsActual.item(j);
        		actual.append("#I##").append(j).append("#").
        			  append("#NODENAME##").append(curr.getNodeName()).append("#").
        			  append("#NODETEXT##").append(curr.getTextContent()).append("#").
        			  append("\n");
        	}
         }
    	expected.append("\n");
    	actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
    public void testDocGetElementByTagName() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuffer expected = new StringBuffer();
        StringBuffer actual = new StringBuffer();
        
        String tags[] = {"*","country","capital"};
        
        for (int s = 0; s < tags.length; s++){
        	expected.append("#TAG#").append(tags[s]).append("\n");
        	actual.append("#TAG#").append(tags[s]).append("\n");
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
        	String uri = t.getDocumentURI();
        	
        	expected.append("#URI##").append(uri).append("#\n");
        	actual.append("#URI##").append(uri).append("#\n");
        	
        	Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
        	NodeList elementsExpected = doc.getElementsByTagName(tags[s]);
        	for (int j = 0; j < elementsExpected.getLength(); j++) {
        		Node curr = elementsExpected.item(j);
        		expected.append("#I##").append(j).append("#").
        				append("#NODENAME##").append(curr.getNodeName()).append("#").
        				append("#NODETEXT##").append(curr.getTextContent()).append("#").
        				append("\n");
        	}
        		            
            DocumentImpl d = new DocumentImpl(t, 0);
        	NodeList elementsActual = d.getElementsByTagName(tags[s]);
        	for (int j = 0; j < elementsActual.getLength(); j++) {
        		Node curr = elementsActual.item(j);
        		actual.append("#I##").append(j).append("#").
        			  append("#NODENAME##").append(curr.getNodeName()).append("#").
        			  append("#NODETEXT##").append(curr.getTextContent()).append("#").
        			  append("\n");
        	}
         }
    	expected.append("\n");
    	actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
    public void testGetElementByTagNameNS() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuffer expected = new StringBuffer();
        StringBuffer actual = new StringBuffer();
        
        String tags[] = {"*","country","capital"};
        String nss[] = {"*","http://www.w3.org/TR/html4/"};
        
        for (int n = 0; n < nss.length; n++)
        for (int s = 0; s < tags.length; s++){
        	expected.append("#TAG#").append(tags[s]).append("\n");
        	expected.append("#NS#").append(nss[n]).append("\n");
        	actual.append("#TAG#").append(tags[s]).append("\n");
        	actual.append("#NS#").append(nss[n]).append("\n");
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
        	String uri = t.getDocumentURI();
        	
        	expected.append("#URI##").append(uri).append("#\n");
        	actual.append("#URI##").append(uri).append("#\n");
        	
        	Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
        	Element rootExpected = (Element) doc.getFirstChild();
        	NodeList elementsExpected = rootExpected.getElementsByTagNameNS(nss[n],tags[s]);
        	for (int j = 0; j < elementsExpected.getLength(); j++) {
        		Node curr = elementsExpected.item(j);
        		expected.append("#I##").append(j).append("#").
        				append("#NODENAME##").append(curr.getNodeName()).append("#").
        				append("#NODETEXT##").append(curr.getTextContent()).append("#").
        				append("\n");
        	}
        		            
            DocumentImpl d = new DocumentImpl(t, 0);
            ElementImpl rootActual = (ElementImpl) d.getFirstChild();
        	NodeList elementsActual = rootActual.getElementsByTagNameNS(nss[n],tags[s]);
        	for (int j = 0; j < elementsActual.getLength(); j++) {
        		Node curr = elementsActual.item(j);
        		actual.append("#I##").append(j).append("#").
        			  append("#NODENAME##").append(curr.getNodeName()).append("#").
        			  append("#NODETEXT##").append(curr.getTextContent()).append("#").
        			  append("\n");
        	}
         }
    	expected.append("\n");
    	actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
//    public void testJavaDomSample() throws IOException {
//        Document doc = Utils.readXMLasDOMDocument(new File(testData, "doc1.xml"));
//        System.out.println("JAVA DOM Root element :"
//            + doc.getDocumentElement().getNodeName());
//
//        NodeList children = doc.getChildNodes();
//        
//        StringBuilder sb = new StringBuilder();
//        walk(children, sb);
//        System.out.println(sb.toString());
//        for (int temp = 0; temp < children.getLength(); temp++) {
//
//            Node nNode = children.item(temp);
//
//            System.out
//                .println("\nCurrent Element :" + nNode.getNodeName());
//
//            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//                Element eElement = (Element) nNode;
//                System.out.println(eElement.getNodeName());
//                System.out.println("contry id : "
//                    + eElement.getAttribute("id"));
//                System.out.println("Country : "
//                    + eElement.getElementsByTagName("country").item(0)
//                        .getTextContent());
//                System.out.println("Last Name : "
//                    + eElement.getElementsByTagName("lastname").item(0)
//                        .getTextContent());
//                System.out.println("Nick Name : "
//                    + eElement.getElementsByTagName("nickname").item(0)
//                        .getTextContent());
//                System.out.println("Salary : "
//                    + eElement.getElementsByTagName("salary").item(0)
//                        .getTextContent());

//            }
//        }
//    }
    
    private void walkDOM (NodeList nodes, StringBuilder sb) {
        for(int i=0; i<nodes.getLength(); i++) {
            Node child = nodes.item(i);
            sb.append(child.getNodeType()).append("#");
            sb.append(child.getNodeName()).append("#");
            sb.append(child.getNodeValue()).append("#");
            if(child.hasChildNodes()) {
                walkDOM(child.getChildNodes(), sb);
            }
        }
    }
    
    
    public void testNodeNameChildNodes() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuilder expected = new StringBuilder();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
            String uri = t.getDocumentURI();
            expected.append(uri);
            Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            NodeList children = doc.getChildNodes();
            walkDOM(children, expected);
            DocumentImpl d = new DocumentImpl(t, 0);
            NodeList eChildren = d.getChildNodes();
            actual.append(uri);
            walkDOM (eChildren, actual);
            
            expected.append("\n");
            actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
 
    }
    
    private void walkDOMNextSibling(NodeList nodes, StringBuilder sb) {
        if(nodes.getLength() <=0 ) return;
        
        Node child = nodes.item(0);
        while (child != null) {
            sb.append(child.getNodeType()).append("#");
            sb.append(child.getNodeName()).append("#");
            sb.append(child.getNodeValue()).append("#");
            
            walkDOMNextSibling(child.getChildNodes(), sb);
            //next sibling
            child = child.getNextSibling();
        }
    }
    
    private void walkDOMPreviousSibling(NodeList nodes, StringBuilder sb) {
        if(nodes.getLength() <=0 ) return;
        
        Node child = nodes.item(nodes.getLength() - 1);
        while (child != null) {
            sb.append(child.getNodeType()).append("#");
            sb.append(child.getNodeName()).append("#");
            sb.append(child.getNodeValue()).append("#");
            
            walkDOMPreviousSibling(child.getChildNodes(), sb);
            //next sibling
            child = child.getPreviousSibling();
        }
    }
    
    public void testNextSibling() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuilder expected = new StringBuilder();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
            String uri = t.getDocumentURI();
            expected.append(uri);
            Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            NodeList children = doc.getChildNodes();
            walkDOMNextSibling(children, expected);
            DocumentImpl d = new DocumentImpl(t, 0);
            NodeList eChildren = d.getChildNodes();
            actual.append(uri);
            walkDOMNextSibling (eChildren, actual);
            
            expected.append("\n");
            actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
 
    }
    
    public void testPreviousSibling() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuilder expected = new StringBuilder();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
            String uri = t.getDocumentURI();
            expected.append(uri);
            Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            NodeList children = doc.getChildNodes();
            walkDOMPreviousSibling(children, expected);
            DocumentImpl d = new DocumentImpl(t, 0);
            NodeList eChildren = d.getChildNodes();
            actual.append(uri);
            walkDOMPreviousSibling (eChildren, actual);
            
            expected.append("\n");
            actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
    private void walkDOMParent (NodeList nodes, StringBuilder sb) {
        for(int i=0; i<nodes.getLength(); i++) {
            Node child = nodes.item(i);
            sb.append(child.getNodeType()).append("#");
            sb.append(child.getNodeName()).append("'s parent is ");
            sb.append(child.getParentNode().getNodeName()).append("#");
            if(child.hasChildNodes()) {
                walkDOMParent(child.getChildNodes(), sb);
            }
        }
    }
    
    public void testParent() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
                + "3docForest", "00000002"), false);
        assertEquals(3, trees.size());

        StringBuilder expected = new StringBuilder();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
            String uri = t.getDocumentURI();
            expected.append(uri);
            Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            NodeList children = doc.getChildNodes();
            walkDOMParent(children, expected);
            DocumentImpl d = new DocumentImpl(t, 0);
            NodeList eChildren = d.getChildNodes();
            actual.append(uri);
            walkDOMParent (eChildren, actual);
            
            expected.append("\n");
            actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
    
    private void walkDOMTextContent (NodeList nodes, StringBuilder sb) {
        for(int i=0; i<nodes.getLength(); i++) {
            Node n = nodes.item(i);
            sb.append(n.getNodeType()).append("#");
            sb.append(n.getTextContent()).append("#");
            if(n.hasChildNodes()) {
                walkDOMTextContent(n.getChildNodes(), sb);
            }
        }
    }
    
    public void testTextContent() throws IOException {
        List<ExpandedTree> trees = Utils.decodeTreeData(
            new File(testData + System.getProperty("file.separator")
            		+ forest, stand), false);
        assertEquals(num, trees.size());

        StringBuilder expected = new StringBuilder();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < trees.size(); i++) {
            ExpandedTree t = trees.get(i);
            String uri = t.getDocumentURI();
            expected.append(uri);
            Document doc = Utils.readXMLasDOMDocument(new File(testData, uri));
            NodeList children = doc.getChildNodes();
            walkDOMTextContent(children, expected);
            DocumentImpl d = new DocumentImpl(t, 0);
            NodeList eChildren = d.getChildNodes();
            actual.append(uri);
            walkDOMTextContent (eChildren, actual);
            
            expected.append("\n");
            actual.append("\n");
        }
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertEquals(expected.toString(), actual.toString());
    }
}
