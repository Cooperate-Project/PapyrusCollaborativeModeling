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
package org.eclipse.papyrus.compare.uml2.tests.elements;

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
 * @see org.eclipse.papyrus.compare.uml2.tests.elements.ElementsFactory
 * @model kind="package" annotation="http://www.eclipse.org/uml2/2.0.0/UML originalName='ModelElements'"
 * @generated
 */
public interface ElementsPackage extends EPackage {
	/**
	 * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNAME = "elements"; //$NON-NLS-1$

	/**
	 * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/papyrus/compare/test/profile/MyML/ModelElements"; //$NON-NLS-1$

	/**
	 * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_PREFIX = "elm"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	ElementsPackage eINSTANCE = org.eclipse.papyrus.compare.uml2.tests.elements.impl.ElementsPackageImpl
			.init();

	/**
	 * The meta object id for the '{@link org.eclipse.papyrus.compare.uml2.tests.elements.impl.ViewpointImpl
	 * <em>Viewpoint</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.papyrus.compare.uml2.tests.elements.impl.ViewpointImpl
	 * @see org.eclipse.papyrus.compare.uml2.tests.elements.impl.ElementsPackageImpl#getViewpoint()
	 * @generated
	 */
	int VIEWPOINT = 0;

	/**
	 * The feature id for the '<em><b>Base Class</b></em>' reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int VIEWPOINT__BASE_CLASS = 0;

	/**
	 * The feature id for the '<em><b>Purpose</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int VIEWPOINT__PURPOSE = 1;

	/**
	 * The number of structural features of the '<em>Viewpoint</em>' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int VIEWPOINT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Viewpoint</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int VIEWPOINT_OPERATION_COUNT = 0;

	/**
	 * Returns the meta object for class '{@link org.eclipse.papyrus.compare.uml2.tests.elements.Viewpoint
	 * <em>Viewpoint</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Viewpoint</em>'.
	 * @see org.eclipse.papyrus.compare.uml2.tests.elements.Viewpoint
	 * @generated
	 */
	EClass getViewpoint();

	/**
	 * Returns the meta object for the reference
	 * '{@link org.eclipse.papyrus.compare.uml2.tests.elements.Viewpoint#getBase_Class <em>Base Class</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the reference '<em>Base Class</em>'.
	 * @see org.eclipse.papyrus.compare.uml2.tests.elements.Viewpoint#getBase_Class()
	 * @see #getViewpoint()
	 * @generated
	 */
	EReference getViewpoint_Base_Class();

	/**
	 * Returns the meta object for the attribute
	 * '{@link org.eclipse.papyrus.compare.uml2.tests.elements.Viewpoint#getPurpose <em>Purpose</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Purpose</em>'.
	 * @see org.eclipse.papyrus.compare.uml2.tests.elements.Viewpoint#getPurpose()
	 * @see #getViewpoint()
	 * @generated
	 */
	EAttribute getViewpoint_Purpose();

	/**
	 * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ElementsFactory getElementsFactory();

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
		 * '{@link org.eclipse.papyrus.compare.uml2.tests.elements.impl.ViewpointImpl <em>Viewpoint</em>}'
		 * class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see org.eclipse.papyrus.compare.uml2.tests.elements.impl.ViewpointImpl
		 * @see org.eclipse.papyrus.compare.uml2.tests.elements.impl.ElementsPackageImpl#getViewpoint()
		 * @generated
		 */
		EClass VIEWPOINT = eINSTANCE.getViewpoint();

		/**
		 * The meta object literal for the '<em><b>Base Class</b></em>' reference feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference VIEWPOINT__BASE_CLASS = eINSTANCE.getViewpoint_Base_Class();

		/**
		 * The meta object literal for the '<em><b>Purpose</b></em>' attribute feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute VIEWPOINT__PURPOSE = eINSTANCE.getViewpoint_Purpose();

	}

} // ElementsPackage
