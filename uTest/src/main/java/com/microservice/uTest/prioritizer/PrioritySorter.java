package com.microservice.uTest.prioritizer;

import java.util.Comparator;

import com.microservice.uTest.dataStructure.TestFrame;

public class PrioritySorter implements Comparator<TestFrame> {

	@Override
	public int compare(TestFrame o1, TestFrame o2) {
		return Float.compare(o1.getPriority(),o2.getPriority());
	}
}
