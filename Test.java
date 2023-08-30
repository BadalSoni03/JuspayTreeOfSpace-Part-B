import java.util.*;

public class Test extends Thread {
	private static class Node {
		int id , lockedCount;
		boolean isBusy;
		Node parent;
		HashSet<Node> lockedDescNodes;
		ArrayList<Node> childs;

		Node() {
			this.id = id;
			this.lockedCount = 0;
			this.isBusy = false;
			this.parent = null;
			this.lockedDescNodes = new HashSet<>();
			this.childs = new ArrayList<>();
		}

		Node(Node parent) {
			this.id = id;
			this.lockedCount = 0;
			this.isBusy = false;
			this.parent = parent;
			this.lockedDescNodes = new HashSet<>();
			this.childs = new ArrayList<>();
		}
	}

	private static Map<String , Node> stringToNode = new HashMap<String , Node>(); 
	private static class Tree {
      	private static Set<Node> vis = new HashSet<Node>();
		
		private static void buildTree(String[] countries , int m) {
			Node root = new Node();
			stringToNode.put(countries[0] , root); 
			var q = new ArrayDeque<Node>(); 
			q.offer(root); 
			int n = countries.length;
			int idx = 1;
			while (!q.isEmpty() && idx < n) {
				int sz = q.size();
				while (sz-- > 0) {
					Node front = q.poll(); 
					for (int i = 1 ; i <= m && idx < n ; i++) {
						Node newNode = new Node(front);
						stringToNode.put(countries[idx++] , newNode); 
						q.offer(newNode);
						front.childs.add(newNode);
					}
				}
			}
		}

		private static class Semaphore {
			Semaphore() {}

			public void wait(Node node) { 
				// vis.add(node);
				while (node.lockedCount == 0);
				node.lockedCount--;
			}

			public void signal(Node node) {
				node.lockedCount++;
			}
		}
		private static Semaphore mutex = new Semaphore();

		public static boolean lockNode(Node node , int id) {
			vis.add(node);
			if (node.lockedCount > 0 || node.lockedDescNodes.size() > 0) return false;
			
			// loop through the ancestors to check if any of the ancestor is locked or not , and if locked return false
			var parent = node.parent;
			while (parent != null) {
				if (parent.lockedCount > 0) {
					return false;
				}
				parent = parent.parent;
			}

			if (node.isBusy == false) {
				node.lockedCount++; 
				if (node.lockedCount >= 2) {
					node.lockedCount--;
					return false;
				} 
			} 
			mutex.wait(node); 

			// Critical Section starts

			node.isBusy = true; 
			parent = node.parent;
			while (parent != null) {
				if (parent.lockedCount > 0 || parent.lockedDescNodes.size() > 0 || vis.contains(parent)) {
					node.lockedCount--;
					parent = node.parent;
					while (parent != null) { 
                      	if (parent.lockedDescNodes.contains(node))
							parent.lockedDescNodes.remove(node);
						parent = parent.parent;
					} 
					return false;
				}
				parent.lockedDescNodes.add(node);
				parent = parent.parent;
			}
			node.isBusy = false;
			
			// Critical Sections ends

			mutex.signal(node);
			
			if (node.lockedCount == 0) {
				node.lockedCount = 1;
			}
			node.id = id;
			// vis.remove(node); //write this at the last of unlock function before returning true (line 136)
			return true;
		}

		public static boolean unlockNode(Node node , int id) {
			if (node.lockedCount == 0 || node.id != id) return false;
			
			var parent = node.parent;
			while (parent != null) {
				parent.lockedDescNodes.remove(node);
				parent = parent.parent;
			}
	      	node.lockedDescNodes.clear();
			node.lockedCount = 0;
			node.id = 0;

			vis.remove(node); // before it was at last in the lock method (line 119)
			return true;
		}

		public static boolean upgradeNode(Node node , int id) {
			if (node.lockedCount > 0 || node.lockedDescNodes.size() == 0) return false;
			
			var parent = node.parent;
			while (parent != null) {
				if (parent.lockedCount > 0) return false;
				parent = parent.parent;
			}
			for (var lockedChild : node.lockedDescNodes) {
				unlockNode(lockedChild , id);
			}
			lockNode(node , id);
			return true;
		}
	}

	private static String[] str = new String[]{"India" , "World" , "China"};
	private static int idx = 0;
	@Override
	public void run() { 
		System.out.println("Current Thread is : " + currentThread().getName());
		int id = 9;
		var curr = str[idx];
		System.out.println("Node is : " + curr);
		var ans = t.lockNode(stringToNode.get(curr) , 9);
		idx = idx + 1;
		System.out.println(ans);
	}

	private Tree t = new Tree();
	private static Scanner scn;

	// The main method
	public static void main(String[] args) { 
		scn = new Scanner(System.in);
		System.out.println("Starting Code!!");
		int n = 7;
		// int m = scn.nextInt(); 
		// int q = scn.nextInt

		String[] countries = new String[]{"World" , "Asia" , "Africa" , "China" , "India" , "SouthAfrica" , "Egypt"};
		
		Tree.buildTree(countries , 2); 
		var obj = new Test();
		Thread t1 = new Thread(obj , "t1");
		Thread t2 = new Thread(obj , "t2");
		Thread t3 = new Thread(obj , "t3");
		t1.start();
		t2.start();
		t3.start();
	}
}
