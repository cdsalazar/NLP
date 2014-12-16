package edu.uchsc.ccp.opendmap.dev;

/*
 * Created on Oct 23, 2004
 *
 */

import java.util.*;
import edu.stanford.smi.protege.model.*;

/**
 * @author evo
 */

public class MyTest {
	private static final String PROJECT_FILE_NAME = "C:\\Program Files\\Protege_3.1_beta\\examples\\newspaper\\newspaper.pprj";

	public static void main(String[] args) {

		Collection errors = new ArrayList();
		Project project = new Project(PROJECT_FILE_NAME, errors);
		if (errors.size() == 0) {
			KnowledgeBase kb = project.getKnowledgeBase();
			System.out.println("Reporter Frame:" + kb.getFrame("Reporter"));
			System.out.println("Reporter instance:"
					+ kb.getFrame("instance_00039").getName());
			System.out.println("Slot instance?:" 
					+ kb.getFrame("name"));
			Instance areporter = (Instance) kb.getFrame("instance_00039");
			System.out.println("Reporter name: " +
					areporter.getOwnSlotValues((Slot)kb.getFrame("name")));
			Iterator jj = areporter.getDirectTypes().iterator();
			while (jj.hasNext()) {
				Cls acls = (Cls) jj.next();
				System.out.println("Frame direct:" + acls);
				Iterator kk = acls.getSuperclasses().iterator();
				while (kk.hasNext()) {
					Cls asup = (Cls) kk.next();
					System.out.println("Superclass: " + asup);
				}
			}

			Iterator i = kb.getClses().iterator();
			while (i.hasNext()) {
				Cls cls = (Cls) i.next();
				System.out.println("Class: " + cls.getName());
				Iterator j = cls.getDirectInstances().iterator();
				Slot nameSlot = (Slot)kb.getFrame("name");
				while (j.hasNext()) {
					Instance instance = (Instance) j.next();
					System.out.println("  Instance: " + instance.getName() +
							 " " + instance.getOwnSlotValues(nameSlot));
				}
			}
		} else {
			displayErrors(errors);
		}
		// waitForContinue();
	}

	private static void displayErrors(Collection errors) {
		Iterator i = errors.iterator();
		while (i.hasNext()) {
			System.out.println("Error: " + i.next());
		}
	}

//	private static void waitForContinue() {
//		System.out.println("Press <Enter> to continue");
//		try {
//			System.in.read();
//		} catch (Exception e) {
//		}
//	}
}
