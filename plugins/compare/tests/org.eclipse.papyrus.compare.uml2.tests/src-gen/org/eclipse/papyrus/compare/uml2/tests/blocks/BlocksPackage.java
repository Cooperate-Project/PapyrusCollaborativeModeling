/**
 * Copyright (c) 2017 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *      Christian W. Damus - initial API and implementation
 * 
 */
package org.eclipse.papyrus.compare.uml2.tests.blocks;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to
 * represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each operation of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.BlocksFactory
 * @model kind="package" annotation="http://www.eclipse.org/uml2/2.0.0/UML originalName='Blocks'"
 * @generated
 */
public interface BlocksPackage extends EPackage {
	/**
	 * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNAME = "blocks"; //$NON-NLS-1$

	/**
	 * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/papyrus/compare/test/profile/MyML/Blocks"; //$NON-NLS-1$

	/**
	 * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_PREFIX = "blk"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	BlocksPackage eINSTANCE = org.eclipse.papyrus.compare.uml2.tests.blocks.impl.BlocksPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.papyrus.compare.uml2.tests.blocks.impl.BlockImpl
	 * <em>Block</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.impl.BlockImpl
	 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.impl.BlocksPackageImpl#getBlock()
	 * @generated
	 */
	int BLOCK = 0;

	/**
	 * The feature id for the '<em><b>Base Class</b></em>' reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int BLOCK__BASE_CLASS = 0;

	/**
	 * The feature id for the '<em><b>Is Encapsulated</b></em>' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int BLOCK__IS_ENCAPSULATED = 1;

	/**
	 * The number of structural features of the '<em>Block</em>' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int BLOCK_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Block</em>' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int BLOCK_OPERATION_COUNT = 0;

	/**
	 * Returns the meta object for class '{@link org.eclipse.papyrus.compare.uml2.tests.blocks.Block
	 * <em>Block</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Block</em>'.
	 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.Block
	 * @generated
	 */
	EClass getBlock();

	/**
	 * Returns the meta object for the reference
	 * '{@link org.eclipse.papyrus.compare.uml2.tests.blocks.Block#getBase_Class <em>Base Class</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the reference '<em>Base Class</em>'.
	 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.Block#getBase_Class()
	 * @see #getBlock()
	 * @generated
	 */
	EReference getBlock_Base_Class();

	/**
	 * Returns the meta object for the attribute
	 * '{@link org.eclipse.papyrus.compare.uml2.tests.blocks.Block#isEncapsulated <em>Is Encapsulated</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Is Encapsulated</em>'.
	 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.Block#isEncapsulated()
	 * @see #getBlock()
	 * @generated
	 */
	EAttribute getBlock_IsEncapsulated();

	/**
	 * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	BlocksFactory getBlocksFactory();

	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each operation of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("hiding")
	interface Literals {
		/**
		 * The meta object literal for the
		 * '{@link org.eclipse.papyrus.compare.uml2.tests.blocks.impl.BlockImpl <em>Block</em>}' class. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.impl.BlockImpl
		 * @see org.eclipse.papyrus.compare.uml2.tests.blocks.impl.BlocksPackageImpl#getBlock()
		 * @generated
		 */
		EClass BLOCK = eINSTANCE.getBlock();

		/**
		 * The meta object literal for the '<em><b>Base Class</b></em>' reference feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference BLOCK__BASE_CLASS = eINSTANCE.getBlock_Base_Class();

		/**
		 * The meta object literal for the '<em><b>Is Encapsulated</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute BLOCK__IS_ENCAPSULATED = eINSTANCE.getBlock_IsEncapsulated();

	}

} // BlocksPackage
