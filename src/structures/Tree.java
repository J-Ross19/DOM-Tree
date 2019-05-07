package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	/**
	 * Builds the DOM tree from input HTML file, through scanner passed
	 * in to the constructor and stored in the sc field of this object. 
	 * 
	 * The root of the tree that is built is referenced by the root field of this object.
	 */
	public void build() {
		Stack<String> stackOfVals = pushAllVals();
		if (stackOfVals.isEmpty()) {
			return;
		}
		String first = stackOfVals.pop();
		first = first.substring(first.indexOf('<') + 1, first.indexOf('>'));
		TagNode head = new TagNode(first, null, null);
		
		build(stackOfVals, head, true);
		
		root = head;
		
	}
	
	// Push all Strings into stack (But, FIFO like a queue)
	private Stack<String> pushAllVals() {
		Stack<String> temp = new Stack<String>();
		Stack<String> stackOfVals = new Stack<String>();

		while (sc.hasNext()) {
			temp.push(sc.nextLine());
		}
		while (!temp.isEmpty()) {
			stackOfVals.push(temp.pop());
		}
		return stackOfVals;
	}
	
	// Helper method for recursion
	private void build(Stack<String> stackOfVals, TagNode node, boolean canHaveChild) {
		
		//If stack is empty return
		if (stackOfVals.isEmpty()) {
			return;
		}
		
		String current = stackOfVals.pop();
		
		// Closing tag
		// Close the call for the child
		if (current.matches("</[a-zA-Z]+>")) {
			return;
		}
		
		
		// Part of tree (Opening tag or text)
		else {
			
			boolean isOpeningTag = false;
			
			
			// Case 1: Opening Tag
			if (current.matches("<[a-zA-Z]+>")) {
				// Remove tag braces
				current = current.substring(current.indexOf('<') + 1, current.indexOf('>'));
				
				isOpeningTag = true;
				
			}
			
			//Case where Node can have a child
			if (canHaveChild) {
				// Create child
				node.firstChild = new TagNode(current, null, null);
				
				// Call build function on Child
				build(stackOfVals, node.firstChild, isOpeningTag);
				
				// After, call build function on itself (to check for siblings)
				build(stackOfVals, node, false);
			} // Case where Node cannot have a child
			else {
				// Create sibling
				node.sibling = new TagNode(current, null, null);
				
				// Call build function on sibling
				build(stackOfVals, node.sibling, isOpeningTag);
			}
			
		}

	}
	
	
	
	
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		// Tree does not exist
		if (root == null) {
			return;
		}
		replaceTag(oldTag, newTag, root);
	}
	
	// Helper method for recursion
	private void replaceTag(String oldTag, String newTag, TagNode node) {
		// Replace tag if it occurs
		if (node.tag.equals(oldTag)) {
			node.tag = newTag;
		}
		
		// Iterates to sibling
		if (node.sibling != null) {
			replaceTag(oldTag, newTag, node.sibling);
		}
		
		// Iterates to first child
		if (node.firstChild != null) {
			replaceTag(oldTag, newTag, node.firstChild);
		}
		
	}
	
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		// Tree does not exist
		if (root == null) {
			return;
		}
		
		TagNode table = findTable(root);
		
		//If there is no table or an invalid row is chosen, return
		if (table == null || row < 1) {
			return;
		}
		TagNode currentNode = table.firstChild;
		
		// If table has no rows, return
		if (currentNode == null) {
			return;
		}
		
		// Go to desired row
		for (int i = 1; i < row; i++) {		
			currentNode = currentNode.sibling;
			
			// If next row doesn't exist, return
			if (currentNode == null) {
				return;
			}
			
		}
		
		// Iterate to first piece of data
		currentNode = currentNode.firstChild;
		
		while (currentNode != null) {
			// Add node
			TagNode nodeToAdd = new TagNode("b", currentNode.firstChild, null);
			currentNode.firstChild = nodeToAdd;
			
			// Iterate to sibling
			currentNode = currentNode.sibling;
		}
		
		// Done
		return;
	}
	
	// Find node of table
	private TagNode findTable(TagNode current) {
		//Returns node of table
		if (current.tag.equals("table")) {
			return current;
		}
		
		// Iterates to sibling
		if (current.sibling != null) {				
			return findTable(current.sibling);
		}
				
		// Iterates to first child
		if (current.firstChild != null) {
			return findTable(current.firstChild);
		}
		
		// Table npt found
		return null;
		
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		// Tree does not exist
		if (root == null) {
			return;
		}
		// Get rid of tag braces if it contains them
		if (tag.contains("<")) {
			tag = tag.substring(tag.indexOf('<') + 1, tag.indexOf('>'));
		}

		else {
			removeTag(tag, root, null, null);
		}
	}
	
	// Helper method for recursion
	private void removeTag(String tag, TagNode root, TagNode parent, TagNode prevSibling) {
		if (root.tag.equals(tag)) {
			// If for some reason, removing the first tag?
			if (root == this.root) {
				this.root = root.firstChild != null ? root.firstChild: root.sibling;
			}
			else {
				if (tag.equals("ol") || tag.equals("ul")) {
					removeLi(root.firstChild);
				}
				
				TagNode temp = root.firstChild != null ? root.firstChild: root.sibling;
				
				// Case 1: Has a parent
				if (parent != null) {
					// First child takes root's place, unless null. In that case, sibling does
					parent.firstChild = temp;
				}
				// Case 2: Has a previous sibling
				else {
					// First child takes root's place, unless null. In that case, sibling does
					prevSibling.sibling = temp;
				}
				// Case where a leaf node is being removed
				if (temp == null) {
					return;
				}
				// Iterate to last sibling of temp
				while (temp.sibling != null) {
					temp = temp.sibling;
				}
				// Set root's old siblings to be siblings of temp's last sibling
				temp.sibling = root.sibling;
				root = root.firstChild != null ? root.firstChild: root.sibling;
			}
		}
		// Iterate to sibling
		if (root.sibling != null) {
			removeTag(tag, root.sibling, null, root);
		}
		// Iterate to child
		if (root.firstChild != null) {
			removeTag(tag, root.firstChild, root, null);
		}

	}
	
	// Replace all instances of li with p immediately under ol or ul tags
	private void removeLi(TagNode node) {
		if (node == null) {
			return;
		}
		while (node != null) {
			if (node.tag.equals("li")) {
				node.tag = "p";
			}
			// Iterate to sibling
			node = node.sibling;
		}
	}
	
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		// Tree does not exist
		if (root == null) {
			return;
		}
		// Get rid of tag braces if it contains them
		if (tag.contains("<")) {
			tag = tag.substring(tag.indexOf('<') + 1, tag.indexOf('>'));
		}
		addTag(word, tag, root);
	}
	
	//Helper method for recursion
	private void addTag(String word, String tag, TagNode root) {
		// Match the word if is preceded and followed by whitespace (or beginning/end of the string) (?<=(^|\\s)) and (?=(\\s|$)) respectively)
		// Word can contain 0 or 1 of the listed punctuation marks after it ([,:;?!.]?)
		// (?i) means case insensitive match
        String regex = "(?i)(?<=^|\\s)"+ word + "[,:;?!.]?(?=\\s|$)";
        
        // If root contains a match (.*) just matches any characters before and after
		if (root.tag.matches(".*" + regex + ".*")) {
			// Splits on "" if it is followed by a match or preceded by one (splits before and after each word)
	        String[] nodes = root.tag.split("(?<=(" + regex + "))|(?=(" + regex + "))");
	        
	        // Create a reference to current sibling of node
	        TagNode sibling = root.sibling;
	        
	        // nodes must contain at least 1 index, since a match was already found
	        if (nodes[0].matches(regex)) {
	        	// Replace original node with tag and make its child the match
	        	root.tag = tag;
	        	root.firstChild = new TagNode(nodes[0], null, null);
	        }
	        else {
	        	// Replace original node with first string (chars before a match)
	        	root.tag = nodes[0];
	        }
	        // Iterate through rest of array
	        for (int i = 1; i < nodes.length; i++) {
	        	// For a match, make the sibling the tag and it's child the match
	        	if (nodes[i].matches(regex)) {
	        		root.sibling = new TagNode(tag, new TagNode(nodes[i], null, null), null);
	        	}
	        	// Otherwise, just make the sibling the substring that was not matched
	        	else {
	        		root.sibling = new TagNode(nodes[i], null, null);
	        	}
	        	// Iterate to next sibling
	        	root = root.sibling;
	        }
	        // Set final node's sibling to be what was originally the old node's sibling
	        root.sibling = sibling;
	        // Change root to continue with recursion checking
	        root = sibling;
	        // Check if null to prevent NullPointerException. If null, there are no more siblings or children, so return
	        if (root == null) {
	        	return;
	        }
		}
		
		if (root.firstChild != null) {
			// Iterate to first child
			addTag(word, tag, root.firstChild);
		}
		if (root.sibling != null) {
			// Iterate to sibling
			addTag(word, tag, root.sibling);
		}
		
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
	/**
	 * Prints the DOM tree. 
	 *
	 */
	public void print() {
		print(root, 1);
	}
	
	private void print(TagNode root, int level) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			for (int i=0; i < level-1; i++) {
				System.out.print("      ");
			};
			if (root != this.root) {
				System.out.print("|----");
			} else {
				System.out.print("     ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level+1);
			}
		}
	}
	
	
	
}
