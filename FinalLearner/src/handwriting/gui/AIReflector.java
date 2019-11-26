package handwriting.gui;


import handwriting.core.SampleData;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("rawtypes")
public class AIReflector<T> {
	private final static String suffix = ".class";
	
	private Map<String,Class> name2type;
	
	private FilenameFilter filter = new FilenameFilter(){public boolean accept(File dir, String name) {
			return name.endsWith(suffix);
		}};
		
	public AIReflector(Class superType, String packageName) {
		this.name2type = new TreeMap<String,Class>();
		
		String targetDirName = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		File targetDir = new File(targetDirName + packageName.replace('.', File.separatorChar));
		if (!targetDir.isDirectory()) {throw new IllegalArgumentException(targetDir + " is not a directory");}
		for (File f: targetDir.listFiles(filter)) {
			String name = f.getName();
			name = name.substring(0, name.length() - suffix.length());
			try {
				System.out.println("name:" + name);
				Class type = Class.forName(packageName + "." + name);
				Object obj = type.getConstructor(SampleData.class).newInstance(new SampleData());
				if (superType.isInstance(obj)) {
					name2type.put(name, type);
					System.out.println("Identified");
				} else {
					System.out.println("Not instance of " + superType.getCanonicalName());
				}
				// If an exception is thrown, we omit the type.
				// Hence, ignore most of these exceptions.
			} catch (ClassNotFoundException e) {
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			} catch (InvocationTargetException e) {
				// If an exception is thrown in the constructor, it is important to know about it.
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<String> getTypeNames() {
		return new ArrayList<String>(name2type.keySet());
	}
	
	public String toString() {
		String result = "Available:";
		for (String s: name2type.keySet()) {
			result += " " + s;
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public T newInstanceOf(String typeName, SampleData training) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return (T)name2type.get(typeName).getConstructor(SampleData.class).newInstance(training);
	}
}