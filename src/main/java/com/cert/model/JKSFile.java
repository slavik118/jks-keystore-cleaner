package com.cert.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = {"pathToStore", "passwordArray"})
public class JKSFile {
	private String pathToStore;
	private char[] passwordArray;
}
