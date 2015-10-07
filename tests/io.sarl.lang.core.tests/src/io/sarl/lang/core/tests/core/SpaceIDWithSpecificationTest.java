/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2015 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.lang.core.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;
import io.sarl.tests.api.AbstractSarlTest;
import io.sarl.tests.api.Nullable;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SpaceIDWithSpecificationTest extends AbstractSarlTest {

	@Nullable
	private UUID contextID;
	@Nullable
	private UUID id;
	@Nullable
	private Class specification;
	@Nullable
	private SpaceID spaceID;

	private int cmp(UUID a, UUID b) {
		int cmp = this.contextID.compareTo(a);
		if (cmp == 0) {
			cmp = this.id.compareTo(b);
		}
		return cmp;
	}

	/**
	 */
	@Before
	public void setUp() {
		this.contextID = UUID.randomUUID();
		this.id = UUID.randomUUID();
		this.specification = SpaceSpecification.class;
		this.spaceID = new SpaceID(this.contextID, this.id, this.specification);
	}

	/**
	 */
	@Test
	public void getID() {
		assertSame(this.id, this.spaceID.getID());
	}

	/**
	 */
	@Test
	public void getContextID() {
		assertSame(this.contextID, this.spaceID.getContextID());
	}

	/**
	 */
	@Test
	public void getSpaceSpecification() {
		assertSame(this.specification, this.spaceID.getSpaceSpecification());
	}

	/**
	 */
	@Test
	public void equals() {
		SpaceID sid;
		//
		assertTrue(this.spaceID.equals(this.spaceID));
		//
		sid = new SpaceID(this.contextID, this.id, this.specification);
		assertTrue(this.spaceID.equals(sid));
		//
		sid = new SpaceID(
				UUID.fromString(this.contextID.toString()),
				UUID.fromString(this.id.toString()),
				this.specification);
		assertTrue(this.spaceID.equals(sid));
		//
		sid = new SpaceID(UUID.randomUUID(), this.id, this.specification);
		assertFalse(this.spaceID.equals(sid));
		//
		sid = new SpaceID(this.contextID, UUID.randomUUID(), this.specification);
		assertFalse(this.spaceID.equals(sid));
		//
		sid = new SpaceID(UUID.randomUUID(), UUID.randomUUID(), this.specification);
		assertFalse(this.spaceID.equals(sid));
	}

	/**
	 */
	@Test
	public void testHashCode() {
		SpaceID sid;
		//
		assertEquals(this.spaceID.hashCode(), this.spaceID.hashCode());
		//
		sid = new SpaceID(this.contextID, this.id, this.specification);
		assertEquals(this.spaceID.hashCode(), sid.hashCode());
		//
		sid = new SpaceID(
				UUID.fromString(this.contextID.toString()),
				UUID.fromString(this.id.toString()),
				this.specification);
		assertEquals(this.spaceID.hashCode(), sid.hashCode());
		//
		sid = new SpaceID(UUID.randomUUID(), this.id, this.specification);
		assertNotEquals(this.spaceID.hashCode(), sid.hashCode());
		//
		sid = new SpaceID(this.contextID, UUID.randomUUID(), this.specification);
		assertNotEquals(this.spaceID.hashCode(), sid.hashCode());
		//
		sid = new SpaceID(UUID.randomUUID(), UUID.randomUUID(), this.specification);
		assertNotEquals(this.spaceID.hashCode(), sid.hashCode());
	}

	/**
	 */
	@Test
	public void compareTo() {
		SpaceID sid;
		UUID uuid1;
		UUID uuid2;
		//
		assertEquals(0, this.spaceID.compareTo(this.spaceID));
		//
		sid = new SpaceID(this.contextID, this.id, this.specification);
		assertEquals(0, this.spaceID.compareTo(sid));
		//
		sid = new SpaceID(
				UUID.fromString(this.contextID.toString()),
				UUID.fromString(this.id.toString()),
				this.specification);
		assertEquals(0, this.spaceID.compareTo(sid));
		//
		uuid1 = UUID.randomUUID();
		sid = new SpaceID(uuid1, this.id, this.specification);
		assertEquals(
				cmp(uuid1, this.id),
				this.spaceID.compareTo(sid));
		//
		uuid1 = UUID.randomUUID();
		sid = new SpaceID(this.contextID, uuid1, this.specification);
		assertEquals(
				cmp(this.contextID, uuid1),
				this.spaceID.compareTo(sid));
		//
		uuid1 = UUID.randomUUID();
		uuid2 = UUID.randomUUID();
		sid = new SpaceID(uuid1, uuid2, this.specification);
		assertEquals(
				cmp(uuid1, uuid2),
				this.spaceID.compareTo(sid));
	}

}
