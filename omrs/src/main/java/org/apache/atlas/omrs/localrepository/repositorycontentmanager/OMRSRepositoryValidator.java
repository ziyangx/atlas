/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.omrs.localrepository.repositorycontentmanager;

import org.apache.atlas.omrs.ffdc.OMRSErrorCode;
import org.apache.atlas.omrs.ffdc.exception.*;
import org.apache.atlas.omrs.metadatacollection.properties.MatchCriteria;
import org.apache.atlas.omrs.metadatacollection.properties.instances.*;
import org.apache.atlas.omrs.metadatacollection.properties.typedefs.*;
import org.apache.atlas.omrs.metadatacollection.repositoryconnector.OMRSRepositoryConnector;
import sun.misc.Regexp;

import javax.lang.model.type.UnknownTypeException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * OMRSRepositoryValidator provides methods to validate TypeDefs and Instances returned from
 * an open metadata repository.  It is typically used by OMRS repository connectors and
 * repository event mappers.
 */
public class OMRSRepositoryValidator implements OMRSTypeDefValidator, OMRSInstanceValidator
{
    private static OMRSRepositoryContentManager    defaultRepositoryContentManager = null;

    private        OMRSRepositoryContentManager    repositoryContentManager;


    /**
     * Default constructor - deprecated as a repository connector should get its repository validator
     * from its superclass.
     */
    @Deprecated
    public OMRSRepositoryValidator()
    {
        repositoryContentManager = defaultRepositoryContentManager;
    }


    /**
     * Typical constructor used by the OMRS to create a repository validator for a repository connector.
     *
     * @param repositoryContentManager - holds details of valid types and provides the implementation of
     *                                 the repository validator methods
     */
    public OMRSRepositoryValidator(OMRSRepositoryContentManager repositoryContentManager)
    {
        this.repositoryContentManager = repositoryContentManager;
    }

    /**
     * Set up the local repository's content manager.  This maintains a cache of the local repository's type
     * definitions and rules to provide helpers and validators for TypeDefs and instances that are
     * exchanged amongst the open metadata repositories and open metadata access services (OMAS).
     *
     * @param repositoryContentManager - link to repository content manager.
     */
    public static synchronized void setRepositoryContentManager(OMRSRepositoryContentManager  repositoryContentManager)
    {
        OMRSRepositoryHelper.setRepositoryContentManager(repositoryContentManager);
    }


    /**
     * Return a summary list of the TypeDefs supported by the local metadata repository.  This is
     * broadcast to the other servers/repositories in the cluster during the membership registration exchanges
     * managed by the cluster registries.
     *
     * @return TypeDefSummary iterator
     */
    public ArrayList<TypeDefSummary> getLocalTypeDefs()
    {
        final String  methodName = "getLocalTypeDefs()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getLocalTypeDefs();
    }


    /**
     * Return a boolean flag indicating whether the list of TypeDefs passed are compatible with the
     * local metadata repository.  A true response means it is ok; false means conflicts have been found.
     *
     * A valid TypeDef is one that:
     * <ul>
     *     <li>
     *         Matches name, GUID and version to a TypeDef in the local repository, or
     *     </li>
     *     <li>
     *         Is not defined in the local repository.
     *     </li>
     * </ul>
     *
     * @param sourceName - name of the caller
     * @param typeDefSummaries - list of summary information about the TypeDefs.
     */
    public void validateAgainstLocalTypeDefs(String               sourceName,
                                             List<TypeDefSummary> typeDefSummaries)
    {
        final String  methodName = "validateAgainstLocalTypeDefs()";

        validateRepositoryContentManager(methodName);

        repositoryContentManager.validateAgainstLocalTypeDefs(sourceName, typeDefSummaries);
    }


    /**
     * Return a boolean flag indicating whether the list of TypeDefs passed are compatible with the
     * all known typedefs.
     *
     * A valid TypeDef is one that matches name, GUID and version to the full list of TypeDefs.
     * If a new TypeDef is present, it is added to the enterprise list.
     *
     * @param typeDefs - list of TypeDefs.
     * @return boolean flag
     */
    public boolean   validateEnterpriseTypeDefs(String             sourceName,
                                                List<TypeDef>      typeDefs)
    {
        final String  methodName = "validateEnterpriseTypeDefs()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validateEnterpriseTypeDefs(sourceName, typeDefs);
    }


    /**
     * Return boolean indicating whether the TypeDef is in use in the repository.
     *
     * @param sourceName - name of caller
     * @param typeDefGUID - unique identifier of the type
     * @param typeDefName - unique name of the type
     * @return boolean flag
     */
    public boolean isActiveType(String   sourceName, String   typeDefGUID, String   typeDefName)
    {
        final String  methodName = "isActiveType()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.isActiveType(sourceName, typeDefGUID, typeDefName);
    }

    /**
     * Return boolean indicating whether the TypeDef is one of the open metadata types.
     *
     * @param sourceName - name of caller
     * @param typeDefGUID - unique identifier of the type
     * @param typeDefName - unique name of the type
     * @return boolean flag
     */
    public boolean isOpenType(String   sourceName, String   typeDefGUID, String   typeDefName)
    {
        final String  methodName = "isOpenType()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.isOpenType(sourceName, typeDefGUID, typeDefName);
    }


    /**
     * Return boolean indicating whether the TypeDef is in use in the repository.
     *
     * @param sourceName - name of caller
     * @param typeDefGUID - unique identifier of the type
     * @param typeDefName - unique name of the type
     * @return boolean flag
     */
    public boolean isKnownType(String   sourceName, String   typeDefGUID, String   typeDefName)
    {
        final String  methodName = "isKnownType()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.isKnownType(sourceName, typeDefGUID, typeDefName);
    }


    /**
     * Return boolean indicating whether the TypeDef identifiers are from a single known type or not.
     *
     * @param sourceName - source of the request (used for logging)
     * @param typeDefGUID - unique identifier of the TypeDef
     * @param typeDefName - unique name of the TypeDef
     * @return boolean result
     */
    public boolean validTypeId(String          sourceName,
                               String          typeDefGUID,
                               String          typeDefName)
    {
        final String  methodName = "validTypeId()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validTypeId(sourceName, typeDefGUID, typeDefName);
    }


    /**
     * Return boolean indicating whether the TypeDef identifiers are from a single known type or not.
     *
     * @param sourceName - source of the request (used for logging)
     * @param typeDefGUID - unique identifier of the TypeDef
     * @param typeDefName - unique name of the TypeDef
     * @param category - category for the TypeDef
     * @return boolean result
     */
    public boolean validTypeDefId(String          sourceName,
                                  String          typeDefGUID,
                                  String          typeDefName,
                                  TypeDefCategory category)
    {
        final String  methodName = "validTypeDefId()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validTypeDefId(sourceName, typeDefGUID, typeDefName, category);
    }

    /**
     * Return boolean indicating whether the AttributeTypeDef identifiers are from a single known type or not.
     *
     * @param sourceName - source of the request (used for logging)
     * @param attributeTypeDefGUID - unique identifier of the AttributeTypeDef
     * @param attributeTypeDefName - unique name of the AttributeTypeDef
     * @param category - category for the AttributeTypeDef
     * @return boolean result
     */
    public boolean validAttributeTypeDefId(String                   sourceName,
                                           String                   attributeTypeDefGUID,
                                           String                   attributeTypeDefName,
                                           AttributeTypeDefCategory category)
    {
        final String  methodName = "validAttributeTypeDefId()";

        validateRepositoryContentManager(methodName);

       return repositoryContentManager.validAttributeTypeDefId(sourceName,
                                                               attributeTypeDefGUID,
                                                               attributeTypeDefName,
                                                               category);
    }



    /**
     * Return boolean indicating whether the TypeDef identifiers are from a single known type or not.
     *
     * @param sourceName - source of the request (used for logging)
     * @param typeDefGUID - unique identifier of the TypeDef
     * @param typeDefName - unique name of the TypeDef
     * @param typeDefVersion - versionName of the type
     * @param category - category for the TypeDef
     * @return boolean result
     */
    public boolean validTypeDefId(String          sourceName,
                                  String          typeDefGUID,
                                  String          typeDefName,
                                  long            typeDefVersion,
                                  TypeDefCategory category)
    {
        final String  methodName = "validTypeDefId()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validTypeDefId(sourceName,
                                                       typeDefGUID,
                                                       typeDefName,
                                                       typeDefVersion,
                                                       category);
    }


    /**
     * Return boolean indicating whether the TypeDef identifiers are from a single known type or not.
     *
     * @param sourceName - source of the request (used for logging)
     * @param attributeTypeDefGUID - unique identifier of the TypeDef
     * @param attributeTypeDefName - unique name of the TypeDef
     * @param attributeTypeDefVersion - versionName of the type
     * @param category - category for the TypeDef
     * @return boolean result
     */
    public boolean validAttributeTypeDefId(String                   sourceName,
                                           String                   attributeTypeDefGUID,
                                           String                   attributeTypeDefName,
                                           long                     attributeTypeDefVersion,
                                           AttributeTypeDefCategory category)
    {
        final String  methodName = "validAttributeTypeDefId()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validAttributeTypeDefId(sourceName,
                                                                attributeTypeDefGUID,
                                                                attributeTypeDefName,
                                                                attributeTypeDefVersion,
                                                                category);
    }



    /**
     * Return boolean indicating whether the supplied TypeDef is valid or not.
     *
     * @param sourceName - source of the TypeDef (used for logging)
     * @param typeDef - TypeDef to test
     * @return boolean result
     */
    public boolean validTypeDef(String         sourceName,
                                TypeDef        typeDef)
    {
        final String methodName = "validTypeDef()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validTypeDef(sourceName, typeDef);
    }


    /**
     * Return boolean indicating whether the supplied AttributeTypeDef is valid or not.
     *
     * @param sourceName - source of the request (used for logging)
     * @param attributeTypeDef - TypeDef to test
     * @return boolean result
     */
    public boolean validAttributeTypeDef(String           sourceName,
                                         AttributeTypeDef attributeTypeDef)
    {
        final String  methodName = "validAttributeTypeDef()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validAttributeTypeDef(sourceName, attributeTypeDef);
    }


    /**
     * Return boolean indicating whether the supplied TypeDefSummary is valid or not.
     *
     * @param sourceName - source of the TypeDefSummary (used for logging)
     * @param typeDefSummary - TypeDefSummary to test.
     * @return boolean result.
     */
    public boolean validTypeDefSummary(String                sourceName,
                                       TypeDefSummary        typeDefSummary)
    {
        final String  methodName = "validTypeDefSummary()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validTypeDefSummary(sourceName, typeDefSummary);
    }


    /**
     * Test that the supplied entity is valid.
     *
     * @param sourceName - source of the entity (used for logging)
     * @param entity - entity to test
     * @return boolean result
     */
    public boolean validEntity(String       sourceName,
                               EntityDetail entity)
    {
        final String  methodName = "validEntity()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validEntity(sourceName, entity);
    }


    /**
     * Test that the supplied relationship is valid.
     *
     * @param sourceName - name of the caller (used for logging)
     * @param relationship - relationship to test
     * @return boolean result
     */
    public boolean validRelationship(String       sourceName,
                                     Relationship relationship)
    {
        final String  methodName = "validRelationship()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validRelationship(sourceName, relationship);
    }


    /**
     * Verify that the identifiers for an instance are correct.
     *
     * @param sourceName - source of the instance (used for logging)
     * @param typeDefGUID - unique identifier for the type.
     * @param typeDefName - unique name for the type.
     * @param category - expected category of the instance.
     * @param instanceGUID - unique identifier for the instance.
     * @return boolean indicating whether the identifiers are ok.
     */
    public boolean validInstanceId(String           sourceName,
                                   String           typeDefGUID,
                                   String           typeDefName,
                                   TypeDefCategory  category,
                                   String           instanceGUID)
    {
        final String  methodName = "validInstanceId()";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.validInstanceId(sourceName,
                                                        typeDefGUID,
                                                        typeDefName,
                                                        category,
                                                        instanceGUID);
    }

    /* ==============================================================
     * Simple parameter validation methods needed by all repositories
     * ==============================================================
     */


    /**
     * Validate that the supplied user Id is not null.
     *
     * @param userId - userId passed on call to this metadata collection
     * @param methodName - name of method requesting the validation.
     * @throws UserNotAuthorizedException - the userId is invalid
     */
    public  void validateUserId(String  repositoryName,
                                String  userId,
                                String  methodName) throws UserNotAuthorizedException
    {
        if ("".equals(userId))
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NULL_USER_ID;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage("userId", methodName, repositoryName);

            throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                                 this.getClass().getName(),
                                                 methodName,
                                                 errorMessage,
                                                 errorCode.getSystemAction(),
                                                 errorCode.getUserAction());
        }
    }


    /**
     * Validate that a TypeDef's identifiers are not null.
     *
     * @param repositoryName - name of the repository.
     * @param guidParameterName - name of the parameter that passed the guid.
     * @param nameParameterName - name of the parameter that passed the name.
     * @param guid - unique identifier for a type or an instance passed on the request
     * @param name - name of TypeDef.
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid provided
     */
    public  void validateTypeDefIds(String repositoryName,
                                    String guidParameterName,
                                    String nameParameterName,
                                    String guid,
                                    String name,
                                    String methodName) throws InvalidParameterException
    {
        if (guid == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF_IDENTIFIER;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(guidParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
        else if (name == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_TYPEDEF_NAME;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(nameParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that an AttributeTypeDef's identifiers are not null and are recognized.
     *
     * @param repositoryName - name of the repository.
     * @param guidParameterName - name of the parameter that passed the guid.
     * @param nameParameterName - name of the parameter that passed the name.
     * @param guid - unique identifier for a type or an instance passed on the request
     * @param name - name of TypeDef.
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid, or name provided
     */
    public  void validateAttributeTypeDefIds(String repositoryName,
                                             String guidParameterName,
                                             String nameParameterName,
                                             String guid,
                                             String name,
                                             String methodName) throws InvalidParameterException
    {
        if (guid == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_ATTRIBUTE_TYPEDEF_IDENTIFIER;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(guidParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
        else if (name == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_ATTRIBUTE_TYPEDEF_NAME;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(nameParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that type's identifier is not null.
     *
     * @param repositoryName - name of the repository.
     * @param guidParameterName - name of the parameter that passed the guid.
     * @param guid - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid provided
     */
    public  void validateTypeGUID(String repositoryName,
                                  String guidParameterName,
                                  String guid,
                                  String methodName) throws InvalidParameterException
    {
        if (guid == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF_IDENTIFIER;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(guidParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    public void validateTypeDefPatch(String       repositoryName,
                                     TypeDefPatch patch,
                                     String       methodName) throws InvalidParameterException, PatchErrorException
    {
        if (patch == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF_PATCH;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that if a type's identifier is passed then it is valid.
     *
     * @param repositoryName - name of the repository.
     * @param guidParameterName - name of the parameter that passed the guid.
     * @param guid - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws TypeErrorException - invalid provided
     */
    public  void validateInstanceTypeGUID(String repositoryName,
                                          String guidParameterName,
                                          String guid,
                                          String methodName) throws TypeErrorException
    {
        if (guid == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.BAD_TYPEDEF_ID_FOR_INSTANCE;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(guidParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new TypeErrorException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that type's name is not null.
     *
     * @param repositoryName - name of the repository.
     * @param nameParameterName - name of the parameter that passed the name.
     * @param name - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no name provided
     */
    public  void validateTypeName(String repositoryName,
                                  String nameParameterName,
                                  String name,
                                  String methodName) throws InvalidParameterException
    {
        if (name == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_TYPEDEF_NAME;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(nameParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that a TypeDef's category is not null.
     *
     * @param repositoryName - name of the repository.
     * @param nameParameterName - name of the parameter that passed the name.
     * @param category - category of TypeDef
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no name provided
     */
    public  void validateTypeDefCategory(String          repositoryName,
                                         String          nameParameterName,
                                         TypeDefCategory category,
                                         String          methodName) throws InvalidParameterException
    {
        if (category == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_TYPEDEF_CATEGORY;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(nameParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that a AttributeTypeDef's category is not null.
     *
     * @param repositoryName - name of the repository.
     * @param nameParameterName - name of the parameter that passed the name.
     * @param category - category of TypeDef
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no name provided
     */
    public  void validateAttributeTypeDefCategory(String                   repositoryName,
                                                  String                   nameParameterName,
                                                  AttributeTypeDefCategory category,
                                                  String                   methodName) throws InvalidParameterException
    {
        if (category == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_ATTRIBUTE_TYPEDEF_CATEGORY;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(nameParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate the content of a new TypeDef is valid.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the typeDef.
     * @param typeDef - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no typeDef provided
     * @throws InvalidTypeDefException - invalid typeDef provided
     */
    public  void validateTypeDef(String  repositoryName,
                                 String  parameterName,
                                 TypeDef typeDef,
                                 String  methodName) throws InvalidParameterException, InvalidTypeDefException
    {
        if (typeDef == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        validateTypeDefIds(repositoryName,
                           parameterName + ".getGUID",
                           parameterName + ".getName",
                           typeDef.getGUID(),
                           typeDef.getName(),
                           methodName);

        validateTypeDefCategory(repositoryName,
                                parameterName + ".getCategory",
                                typeDef.getCategory(),
                                methodName);


    }


    /**
     * Validate the content of a new TypeDef is known.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the typeDef.
     * @param typeDef - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws TypeDefNotKnownException - no recognized typeDef provided
     */
    public  void validateKnownTypeDef(String  repositoryName,
                                      String  parameterName,
                                      TypeDef typeDef,
                                      String  methodName) throws TypeDefNotKnownException
    {
        final String  thisMethodName = "validateKnownTypeDef";

        validateRepositoryContentManager(thisMethodName);

        if (! repositoryContentManager.isKnownType(repositoryName, typeDef.getGUID(), typeDef.getName()))
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.TYPEDEF_NOT_KNOWN;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(typeDef.getName(),
                                                                            typeDef.getGUID(),
                                                                            parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new TypeDefNotKnownException(errorCode.getHTTPErrorCode(),
                                               this.getClass().getName(),
                                               methodName,
                                               errorMessage,
                                               errorCode.getSystemAction(),
                                               errorCode.getUserAction());

        }
    }


    /**
     * Validate the content of a new TypeDef is known.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the typeDef.
     * @param typeDef - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws TypeDefKnownException - the TypeDef is already defined
     * @throws TypeDefConflictException - the TypeDef is already defined - but differently
     */
    public  void validateUnknownTypeDef(String  repositoryName,
                                        String  parameterName,
                                        TypeDef typeDef,
                                        String  methodName) throws TypeDefKnownException,
                                                                   TypeDefConflictException
    {
        final String  thisMethodName = "validateUnknownTypeDef";

        validateRepositoryContentManager(thisMethodName);

        if (repositoryContentManager.isKnownType(repositoryName, typeDef.getGUID(), typeDef.getName()))
        {
            // todo validate that the existing typeDef matches the new one.

            OMRSErrorCode errorCode = OMRSErrorCode.TYPEDEF_ALREADY_DEFINED;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(typeDef.getName(),
                                                                                                     typeDef.getGUID(),
                                                                                                     repositoryName);

            throw new TypeDefKnownException(errorCode.getHTTPErrorCode(),
                                            this.getClass().getName(),
                                            methodName,
                                            errorMessage,
                                            errorCode.getSystemAction(),
                                            errorCode.getUserAction());

        }
    }


    /**
     * Validate the content of a new TypeDef is known.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the typeDef.
     * @param attributeTypeDef - unique identifier for an attribute type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws TypeDefKnownException - the TypeDef is already defined
     * @throws TypeDefConflictException - the TypeDef is already defined - but differently
     */
    public  void validateUnknownAttributeTypeDef(String           repositoryName,
                                                 String           parameterName,
                                                 AttributeTypeDef attributeTypeDef,
                                                 String           methodName) throws TypeDefKnownException,
                                                                                     TypeDefConflictException
    {
        final String  thisMethodName = "validateUnknownTypeDef";

        validateRepositoryContentManager(thisMethodName);

        if (repositoryContentManager.isKnownType(repositoryName,
                                                 attributeTypeDef.getGUID(),
                                                 attributeTypeDef.getName()))
        {
            // todo validate that the existing typeDef matches the new one.

            OMRSErrorCode errorCode = OMRSErrorCode.ATTRIBUTE_TYPEDEF_ALREADY_DEFINED;
            String errorMessage = errorCode.getErrorMessageId()
                    + errorCode.getFormattedErrorMessage(attributeTypeDef.getName(),
                                                         attributeTypeDef.getGUID(),
                                                         repositoryName);

            throw new TypeDefKnownException(errorCode.getHTTPErrorCode(),
                                            this.getClass().getName(),
                                            methodName,
                                            errorMessage,
                                            errorCode.getSystemAction(),
                                            errorCode.getUserAction());

        }
    }


    /**
     * Validate the content of a TypeDef associated with a metadata instance.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the typeDef.
     * @param typeDef - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws TypeErrorException - no typeDef provided
     * @throws RepositoryErrorException - the TypeDef from the repository is in error.
     */
    public  void validateTypeDefForInstance(String  repositoryName,
                                            String  parameterName,
                                            TypeDef typeDef,
                                            String  methodName) throws TypeErrorException,
                                                                       RepositoryErrorException
    {
        if (typeDef == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new TypeErrorException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        try
        {
            validateTypeDefIds(repositoryName,
                               parameterName + ".getGUID",
                               parameterName + ".getName",
                               typeDef.getGUID(),
                               typeDef.getName(),
                               methodName);

            validateTypeDefCategory(repositoryName,
                                    parameterName + ".getCategory",
                                    typeDef.getCategory(),
                                    methodName);
        }
        catch (Throwable    error)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.BAD_TYPEDEF;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                                               this.getClass().getName(),
                                               methodName,
                                               errorMessage,
                                               errorCode.getSystemAction(),
                                               errorCode.getUserAction());
        }
    }


    /**
     * Validate that the supplied TypeDef GUID and name matches the type associated with a metadata instance.
     *
     * @param repositoryName - name of the repository.
     * @param typeDefGUID - the supplied typeDef GUID.
     * @param typeDefName - the supplied typeDef name.
     * @param instance - instance retrieved from the store with the supplied instance guid
     * @param methodName - method making this call
     * @throws InvalidParameterException - incompatibility detected between the TypeDef and the instance's type
     * @throws RepositoryErrorException - the instance from the repository is in error.
     */
    public  void validateTypeForInstanceDelete(String         repositoryName,
                                               String         typeDefGUID,
                                               String         typeDefName,
                                               InstanceHeader instance,
                                               String         methodName) throws InvalidParameterException,
                                                                                 RepositoryErrorException
    {
        /*
         * Just make sure the instance has a type :)
         */
        this.validateInstanceType(repositoryName, instance);


        /*
         * Both the GUID and the name must match
         */
        if ((! typeDefGUID.equals(instance.getType().getTypeDefGUID())) ||
            (! typeDefName.equals(instance.getType().getTypeDefName())))
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.BAD_TYPEDEF_IDS_FOR_DELETE;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(typeDefName,
                                                                            typeDefGUID,
                                                                            methodName,
                                                                            instance.getGUID(),
                                                                            repositoryName);
            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

    }


    /**
     * Validate the content of a new AttributeTypeDef.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the attributeTypeDef.
     * @param attributeTypeDef - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no attributeTypeDef provided
     * @throws InvalidTypeDefException - bad attributeTypeDef provided
     */
    public  void validateAttributeTypeDef(String           repositoryName,
                                          String           parameterName,
                                          AttributeTypeDef attributeTypeDef,
                                          String           methodName) throws InvalidParameterException,
                                                                              InvalidTypeDefException
    {
        if (attributeTypeDef == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_ATTRIBUTE_TYPEDEF;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        validateAttributeTypeDefIds(repositoryName,
                                    parameterName + ".getGUID",
                                    parameterName + ".getName",
                                    attributeTypeDef.getGUID(),
                                    attributeTypeDef.getName(),
                                    methodName);

        validateAttributeTypeDefCategory(repositoryName,
                                         parameterName + ".getCategory",
                                         attributeTypeDef.getCategory(),
                                         methodName);
    }


    /**
     * Validate that type's name is not null.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the name.
     * @param gallery - typeDef gallery
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no name provided
     */
    public  void validateTypeDefGallery(String         repositoryName,
                                        String         parameterName,
                                        TypeDefGallery gallery,
                                        String         methodName) throws InvalidParameterException
    {
        if (gallery == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF_GALLERY;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that the type's name is not null.
     *
     * @param repositoryName - name of the repository.
     * @param standard - name of the standard - null means any.
     * @param organization - name of the organization - null means any.
     * @param identifier - identifier of the element in the standard - null means any.
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no name provided
     */
    public  void validateExternalId(String repositoryName,
                                    String standard,
                                    String organization,
                                    String identifier,
                                    String methodName) throws InvalidParameterException
    {
        if ((standard == null) && (organization == null) || (identifier == null))
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NO_EXTERNAL_ID;

            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                     repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that an entity's identifier is not null.
     *
     * @param repositoryName - name of the repository.
     * @param guidParameterName - name of the parameter that passed the guid.
     * @param guid - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid provided
     */
    public  void validateGUID(String repositoryName,
                              String guidParameterName,
                              String guid,
                              String methodName) throws InvalidParameterException
    {
        if (guid == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_GUID;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(guidParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that a home metadata collection identifier is not null.
     *
     * @param repositoryName - name of the repository.
     * @param guidParameterName - name of the parameter that passed the guid.
     * @param guid - unique identifier for a type or an instance passed on the request
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid provided
     */
    public  void validateHomeMetadataGUID(String repositoryName,
                                          String guidParameterName,
                                          String guid,
                                          String methodName) throws InvalidParameterException
    {
        if (guid == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_HOME_METADATA_COLLECTION_ID;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(guidParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that a classification name is not null.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the guid.
     * @param classificationName - unique name for a classification type
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - classification name is null
     */
    public  void validateClassificationName(String repositoryName,
                                            String parameterName,
                                            String classificationName,
                                            String methodName) throws InvalidParameterException
    {
        if (classificationName == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_CLASSIFICATION_NAME;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that a classification is valid for the entity.
     *
     * @param repositoryName - name of the repository.
     * @param classificationName - unique name for a classification type
     * @param propertiesParameterName - name of the parameter that passed the properties.
     * @param classificationProperties - properties to test
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - classification name is null
     * @throws TypeErrorException - the classification is invalid for this entity
     */
    public  void validateClassificationProperties(String             repositoryName,
                                                  String             classificationName,
                                                  String             propertiesParameterName,
                                                  InstanceProperties classificationProperties,
                                                  String             methodName) throws PropertyErrorException,
                                                                                        TypeErrorException
    {
        if (repositoryContentManager != null)
        {
            TypeDef   classificationTypeDef = repositoryContentManager.getTypeDefByName(repositoryName, classificationName);

            if (classificationTypeDef != null)
            {
                validatePropertiesForType(repositoryName, propertiesParameterName, classificationTypeDef, classificationProperties, methodName);
            }
            else
            {
                /*
                 * Logic error as the type should be valid
                 */
                final String   thisMethodName = "validateClassificationProperties";

                throwValidatorLogicError(repositoryName, methodName, thisMethodName);
            }
        }
        else
        {
            OMRSErrorCode errorCode = OMRSErrorCode.LOCAL_REPOSITORY_CONFIGURATION_ERROR;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Validate that a classification is valid for the entity.
     *
     * @param repositoryName - name of the repository.
     * @param classificationParameterName - name of the parameter that passed the guid.
     * @param classificationName - unique name for a classification type
     * @param entityTypeName - name of entity type
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - classification name is null
     * @throws ClassificationErrorException - the classification is invalid for this entity
     */
    public  void validateClassification(String             repositoryName,
                                        String             classificationParameterName,
                                        String             classificationName,
                                        String             entityTypeName,
                                        String             methodName) throws InvalidParameterException,
                                                                              ClassificationErrorException
    {
        this.validateClassificationName(repositoryName, classificationParameterName, classificationName, methodName);

        if (repositoryContentManager != null)
        {
            if (! repositoryContentManager.isValidClassificationForEntity(repositoryName, classificationName, entityTypeName))
            {
                OMRSErrorCode errorCode    = OMRSErrorCode.INVALID_CLASSIFICATION_FOR_ENTITY;
                String        errorMessage = errorCode.getErrorMessageId()
                                           + errorCode.getFormattedErrorMessage(repositoryName,
                                                                                classificationName,
                                                                                entityTypeName);

                throw new ClassificationErrorException(errorCode.getHTTPErrorCode(),
                                                       this.getClass().getName(),
                                                       methodName,
                                                       errorMessage,
                                                       errorCode.getSystemAction(),
                                                       errorCode.getUserAction());
            }
        }
        else
        {
            OMRSErrorCode errorCode = OMRSErrorCode.LOCAL_REPOSITORY_CONFIGURATION_ERROR;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Validate that a classification is valid for the entity.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the guid.
     * @param classifications - list of classifications
     * @param entityTypeName - name of entity type
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - classification name is null
     * @throws ClassificationErrorException - the classification is invalid for this entity
     * @throws PropertyErrorException - the classification's properties are invalid for its type
     * @throws TypeErrorException - the classification's type is invalid
     */
    public  void validateClassificationList(String               repositoryName,
                                            String               parameterName,
                                            List<Classification> classifications,
                                            String               entityTypeName,
                                            String               methodName) throws InvalidParameterException,
                                                                                    ClassificationErrorException,
                                                                                    PropertyErrorException,
                                                                                    TypeErrorException
    {
        if (classifications != null)
        {
            for (Classification classification : classifications)
            {
                if (classification != null)
                {
                    if (repositoryContentManager != null)
                    {
                        this.validateClassification(repositoryName,
                                                    parameterName,
                                                    classification.getName(),
                                                    entityTypeName,
                                                    methodName);


                        this.validatePropertiesForType(repositoryName,
                                                       parameterName,
                                                       repositoryContentManager.getTypeDefByName(repositoryName,
                                                                                                 classification.getName()),
                                                       classification.getProperties(),
                                                       methodName);
                    }
                    else
                    {
                        OMRSErrorCode errorCode = OMRSErrorCode.LOCAL_REPOSITORY_CONFIGURATION_ERROR;
                        String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

                        throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                                          this.getClass().getName(),
                                                          methodName,
                                                          errorMessage,
                                                          errorCode.getSystemAction(),
                                                          errorCode.getUserAction());
                    }

                }
                else
                {
                    OMRSErrorCode errorCode    = OMRSErrorCode.NULL_CLASSIFICATION_NAME;
                    String        errorMessage = errorCode.getErrorMessageId()
                                               + errorCode.getFormattedErrorMessage(parameterName,
                                                                                    methodName,
                                                                                    repositoryName);

                    throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                        this.getClass().getName(),
                                                        methodName,
                                                        errorMessage,
                                                        errorCode.getSystemAction(),
                                                        errorCode.getUserAction());
                }
            }
        }
    }


    /**
     * Validate that a TypeDef match criteria set of properties is not null.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the match criteria.
     * @param matchCriteria - match criteria properties
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid provided
     */
    public  void validateMatchCriteria(String            repositoryName,
                                       String            parameterName,
                                       TypeDefProperties matchCriteria,
                                       String            methodName) throws InvalidParameterException
    {
        if (matchCriteria == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_MATCH_CRITERIA;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that a metadata instance match criteria and set of properties are either both null or
     * both not null.
     *
     * @param repositoryName - name of the repository.
     * @param matchCriteriaParameterName - name of the parameter that passed the match criteria.
     * @param matchPropertiesParameterName - name of the parameter that passed the match criteria.
     * @param matchCriteria - match criteria enum
     * @param matchProperties - match properties
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid provided
     */
    public  void validateMatchCriteria(String             repositoryName,
                                       String             matchCriteriaParameterName,
                                       String             matchPropertiesParameterName,
                                       MatchCriteria      matchCriteria,
                                       InstanceProperties matchProperties,
                                       String             methodName) throws InvalidParameterException
    {
        if ((matchCriteria == null) && (matchProperties == null))
        {
            return;
        }

        if (matchCriteria == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_MATCH_CRITERIA;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(matchCriteriaParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        if (matchProperties == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_MATCH_CRITERIA;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(matchPropertiesParameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that a search criteria  is not null.
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the parameter that passed the search criteria.
     * @param searchCriteria - match criteria properties
     * @param methodName - method receiving the call
     * @throws InvalidParameterException - no guid provided
     */
    public  void validateSearchCriteria(String repositoryName,
                                        String parameterName,
                                        String searchCriteria,
                                        String methodName) throws InvalidParameterException
    {
        if ((searchCriteria == null) || ("".equals(searchCriteria)))
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_SEARCH_CRITERIA;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(parameterName,
                                                                            methodName,
                                                                            repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validate that the properties for a metadata instance match its TypeDef
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the properties parameter.
     * @param typeDef - type information to validate against.
     * @param properties - proposed properties
     * @param methodName - method receiving the call
     * @throws RepositoryErrorException - no typeDef provided
     * @throws PropertyErrorException - invalid property
     */
    public  void validatePropertiesForType(String             repositoryName,
                                           String             parameterName,
                                           TypeDef            typeDef,
                                           InstanceProperties properties,
                                           String             methodName) throws PropertyErrorException,
                                                                                 TypeErrorException
    {
        if (typeDef == null)
        {
            /*
             * Logic error as the type should be valid
             */
            final String   thisMethodName = "validatePropertiesForType";

            throwValidatorLogicError(repositoryName, methodName, thisMethodName);
        }

        if (properties == null)
        {
            /*
             * No properties to evaluate so return
             */
            return;
        }


        String  typeDefCategoryName = null;
        String  typeDefName         = typeDef.getName();

        if (typeDef.getCategory() != null)
        {
            typeDefCategoryName = typeDef.getCategory().getTypeName();
        }

        List<TypeDefAttribute>  typeDefAttributes = typeDef.getPropertiesDefinition();

        if (typeDefAttributes == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_PROPERTIES_FOR_TYPE;
            String        errorMessage = errorCode.getErrorMessageId()
                                       + errorCode.getFormattedErrorMessage(typeDefCategoryName,
                                                                            typeDefName,
                                                                            repositoryName);

            throw new PropertyErrorException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction());
        }

        /*
         * Need to step through each of the proposed properties and validate that the name and value are
         * present and they match the typeDef
         */
        Iterator    propertyList = properties.getPropertyNames();

        while (propertyList.hasNext())
        {
            String   propertyName = propertyList.next().toString();

            if (propertyName == null)
            {
                OMRSErrorCode errorCode    = OMRSErrorCode.NULL_PROPERTY_NAME_FOR_INSTANCE;
                String        errorMessage = errorCode.getErrorMessageId()
                                           + errorCode.getFormattedErrorMessage(parameterName,
                                                                                methodName,
                                                                                repositoryName);

                throw new PropertyErrorException(errorCode.getHTTPErrorCode(),
                                                 this.getClass().getName(),
                                                 methodName,
                                                 errorMessage,
                                                 errorCode.getSystemAction(),
                                                 errorCode.getUserAction());
            }

            AttributeTypeDefCategory  propertyDefinitionType = null;
            boolean                   recognizedProperty = false;

            for (TypeDefAttribute typeDefAttribute : typeDefAttributes)
            {
                if (typeDefAttribute != null)
                {
                    if (propertyName.equals(typeDefAttribute.getAttributeName()))
                    {
                        recognizedProperty = true;

                        AttributeTypeDef  attributeTypeDef = typeDefAttribute.getAttributeType();
                        if (attributeTypeDef == null)
                        {
                            propertyDefinitionType = AttributeTypeDefCategory.PRIMITIVE;
                        }
                        else
                        {
                            propertyDefinitionType = attributeTypeDef.getCategory();
                        }
                    }
                }
            }

            if (! recognizedProperty)
            {
                OMRSErrorCode errorCode    = OMRSErrorCode.BAD_PROPERTY_FOR_TYPE;
                String        errorMessage = errorCode.getErrorMessageId()
                                           + errorCode.getFormattedErrorMessage(propertyName,
                                                                                typeDefCategoryName,
                                                                                typeDefName,
                                                                                repositoryName);

                throw new PropertyErrorException(errorCode.getHTTPErrorCode(),
                                                 this.getClass().getName(),
                                                 methodName,
                                                 errorMessage,
                                                 errorCode.getSystemAction(),
                                                 errorCode.getUserAction());
            }

            InstancePropertyValue propertyValue = properties.getPropertyValue(propertyName);

            if (propertyValue == null)
            {
                OMRSErrorCode errorCode    = OMRSErrorCode.NULL_PROPERTY_VALUE_FOR_INSTANCE;
                String        errorMessage = errorCode.getErrorMessageId()
                                           + errorCode.getFormattedErrorMessage(parameterName,
                                                                                methodName,
                                                                                repositoryName);

                throw new PropertyErrorException(errorCode.getHTTPErrorCode(),
                                                 this.getClass().getName(),
                                                 methodName,
                                                 errorMessage,
                                                 errorCode.getSystemAction(),
                                                 errorCode.getUserAction());
            }

            InstancePropertyCategory propertyType = propertyValue.getInstancePropertyCategory();

            if (propertyType == null)
            {
                OMRSErrorCode errorCode    = OMRSErrorCode.NULL_PROPERTY_TYPE_FOR_INSTANCE;
                String        errorMessage = errorCode.getErrorMessageId()
                                           + errorCode.getFormattedErrorMessage(parameterName,
                                                                                methodName,
                                                                                repositoryName);

                throw new PropertyErrorException(errorCode.getHTTPErrorCode(),
                                                 this.getClass().getName(),
                                                 methodName,
                                                 errorMessage,
                                                 errorCode.getSystemAction(),
                                                 errorCode.getUserAction());
            }

            boolean  validPropertyType = false;
            String   validPropertyTypeName = propertyType.getTypeName();

            switch (propertyType)
            {
                case PRIMITIVE:
                    if (propertyDefinitionType == AttributeTypeDefCategory.PRIMITIVE)
                    {
                        validPropertyType = true;
                    }
                    break;

                case ENUM:
                    if (propertyDefinitionType == AttributeTypeDefCategory.ENUM_DEF)
                    {
                        validPropertyType = true;
                    }
                    break;

                case MAP:
                    if (propertyDefinitionType == AttributeTypeDefCategory.COLLECTION)
                    {
                        validPropertyType = true;
                    }
                    break;

                case ARRAY:
                    if (propertyDefinitionType == AttributeTypeDefCategory.COLLECTION)
                    {
                        validPropertyType = true;
                    }
                    break;

                case STRUCT:
                    if (propertyDefinitionType == AttributeTypeDefCategory.COLLECTION)
                    {
                        validPropertyType = true;
                    }
                    break;
            }

            if (! validPropertyType)
            {
                OMRSErrorCode errorCode    = OMRSErrorCode.BAD_PROPERTY_TYPE;
                String        errorMessage = errorCode.getErrorMessageId()
                                           + errorCode.getFormattedErrorMessage(propertyName,
                                                                                propertyType.getTypeName(),
                                                                                typeDefCategoryName,
                                                                                typeDefName,
                                                                                validPropertyTypeName,
                                                                                repositoryName);

                throw new PropertyErrorException(errorCode.getHTTPErrorCode(),
                                                 this.getClass().getName(),
                                                 methodName,
                                                 errorMessage,
                                                 errorCode.getSystemAction(),
                                                 errorCode.getUserAction());
            }
        }
    }


    /**
     * Validate that the properties for a metadata instance match its TypeDef
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the properties parameter.
     * @param typeDef - type information to validate against.
     * @param properties - proposed properties
     * @param methodName - method receiving the call
     * @throws RepositoryErrorException - no typeDef provided
     * @throws PropertyErrorException - invalid property
     */
    public  void validatePropertiesForType(String             repositoryName,
                                           String             parameterName,
                                           TypeDefSummary     typeDefSummary,
                                           InstanceProperties properties,
                                           String             methodName) throws PropertyErrorException,
                                                                                 TypeErrorException
    {
        if (typeDefSummary == null)
        {
            /*
             * Logic error as the type should be valid
             */
            final String   thisMethodName = "validatePropertiesForType";

            throwValidatorLogicError(repositoryName, methodName, thisMethodName);
        }

        validateRepositoryContentManager(methodName);

        TypeDef typeDef = repositoryContentManager.getTypeDef(repositoryName,
                                                              typeDefSummary.getGUID(),
                                                              typeDefSummary.getName());

        this.validatePropertiesForType(repositoryName, parameterName, typeDef, properties, methodName);
    }


    /**
     * Validate that the properties for a metadata instance match its TypeDef
     *
     * @param repositoryName - name of the repository.
     * @param parameterName - name of the properties parameter.
     * @param typeDef - type information to validate against.
     * @param properties - proposed properties
     * @param methodName - method receiving the call
     * @throws PropertyErrorException - invalid property
     * @throws RepositoryErrorException - logic error in the repository - corrupted instance
     */
    public  void validateNewPropertiesForType(String             repositoryName,
                                              String             parameterName,
                                              TypeDef            typeDef,
                                              InstanceProperties properties,
                                              String             methodName) throws PropertyErrorException,
                                                                                    RepositoryErrorException
    {
        if (properties != null)
        {
            try
            {
                this.validatePropertiesForType(repositoryName, parameterName, typeDef, properties, methodName);
            }
            catch (TypeErrorException error)
            {
                OMRSErrorCode errorCode = OMRSErrorCode.REPOSITORY_LOGIC_ERROR;
                String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(repositoryName,
                                                                                                         methodName,
                                                                                                         error.getErrorMessage());

                throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                                                  this.getClass().getName(),
                                                  methodName,
                                                  errorMessage,
                                                  errorCode.getSystemAction(),
                                                  errorCode.getUserAction());
            }
        }
        else
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NO_NEW_PROPERTIES;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(parameterName,
                                                                                                     methodName,
                                                                                                     repositoryName);

            throw new PropertyErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Verify whether the instance passed to this method is of the type indicated by the type guid.
     *
     * @param instanceTypeGUID - unique identifier of the type.
     * @param instance - instance to test.
     * @return boolean
     */
    public boolean verifyInstanceType(String           instanceTypeGUID,
                                      InstanceHeader   instance)
    {
        if (instance != null)
        {
            if (instanceTypeGUID != null)
            {
                InstanceType entityType = instance.getType();

                if (entityType != null)
                {
                    if (instanceTypeGUID.equals(entityType.getTypeDefGUID()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     * Verify that the instance retrieved from the repository has a valid instance type.
     *
     * @param repositoryName - name of the repository where the instance came from.
     * @param instance - the retrieved instance
     * @throws RepositoryErrorException - logic error in the repository - corrupted instance
     */
    public void validateInstanceType(String           repositoryName,
                                     InstanceHeader   instance) throws RepositoryErrorException
    {
        final String  methodName = "validateInstanceType";

        if (instance != null)
        {
            InstanceType instanceType = instance.getType();

            if (instanceType != null)
            {
                if (this.isActiveType(repositoryName, instanceType.getTypeDefGUID(), instanceType.getTypeDefName()))
                {
                    return;
                }
                else
                {
                    OMRSErrorCode errorCode = OMRSErrorCode.INACTIVE_INSTANCE_TYPE;
                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                             repositoryName,
                                                                                                             instance.getGUID(),
                                                                                                             instanceType.getTypeDefName(),
                                                                                                             instanceType.getTypeDefGUID());

                    throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                                                       this.getClass().getName(),
                                                       methodName,
                                                       errorMessage,
                                                       errorCode.getSystemAction(),
                                                       errorCode.getUserAction());
                }
            }
            else
            {
                OMRSErrorCode errorCode = OMRSErrorCode.NULL_INSTANCE_TYPE;
                String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                         repositoryName);

                throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                                                   this.getClass().getName(),
                                                   methodName,
                                                   errorMessage,
                                                   errorCode.getSystemAction(),
                                                   errorCode.getUserAction());
            }
        }
        else
        {
            /*
             * Logic error as the instance should be valid
             */
            final String   thisMethodName = "validateInstanceType";

            throwValidatorLogicError(repositoryName, methodName, thisMethodName);
        }
    }

    /**
     * Validate that the supplied type is a valid active type.
     *
     * @param repositoryName - name of the repository where the instance came from.
     * @param typeParameterName - the name of the parameter that passed the type
     * @param typeDefSummary - the type to test
     * @param category - the expected category of the type
     * @param methodName - the name of the method that supplied the type
     * @throws InvalidParameterException - the type is null or contains invalid values
     * @throws TypeErrorException - the type is not active
     */
    public void validateType(String           repositoryName,
                             String           typeParameterName,
                             TypeDefSummary   typeDefSummary,
                             TypeDefCategory  category,
                             String           methodName) throws TypeErrorException, InvalidParameterException
    {
        if (! this.isActiveType(repositoryName, typeDefSummary.getGUID(), typeDefSummary.getName()))
        {
            OMRSErrorCode errorCode = OMRSErrorCode.TYPEDEF_NOT_KNOWN;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(typeDefSummary.getName(),
                                                                                                     typeDefSummary.getGUID(),
                                                                                                     typeParameterName,
                                                                                                     methodName,
                                                                                                     repositoryName);

            throw new TypeErrorException(errorCode.getHTTPErrorCode(),
                                         this.getClass().getName(),
                                         methodName,
                                         errorMessage,
                                         errorCode.getSystemAction(),
                                         errorCode.getUserAction());
        }
    }


    /**
     * Verify that the instance retrieved from the repository has a valid instance type that matches the
     * expected type.
     *
     * @param repositoryName - name of the repository where the instance came from.
     * @param instance - the retrieved instance
     * @throws RepositoryErrorException - logic error in the repository - corrupted instance
     */
    public void validateInstanceType(String           repositoryName,
                                     InstanceHeader   instance,
                                     String           typeGUIDParameterName,
                                     String           typeNameParameterName,
                                     String           expectedTypeGUID,
                                     String           expectedTypeName) throws RepositoryErrorException,
                                                                               TypeErrorException,
                                                                               InvalidParameterException
    {
        final String  methodName = "validateInstanceType";

        this.validateInstanceType(repositoryName, instance);

        if (expectedTypeGUID == null)
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NULL_TYPEDEF_IDENTIFIER;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(typeGUIDParameterName,
                                                                                                     methodName,
                                                                                                     repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        if (expectedTypeName == null)
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NULL_TYPEDEF_NAME;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(typeNameParameterName,
                                                                                                     methodName,
                                                                                                     repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }


    }

    /**
     * Verify that the supplied instance is in one of the supplied statuses.
     *
     * @param validStatuses - list of statuses - the instance should be in any one of them
     * @param instance - instance to test
     * @return boolean result
     */
    public boolean verifyInstanceHasRightStatus(List<InstanceStatus>      validStatuses,
                                                InstanceHeader            instance)
    {
        if ((instance != null) && (validStatuses != null))
        {
            for (InstanceStatus status : validStatuses)
            {
                if (status == instance.getStatus())
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Validates an instance status where null is permissible.
     *
     * @param repositoryName - name of the repository making the request
     * @param instanceStatusParameterName - name of the initial status parameter
     * @param instanceStatus - initial status value
     * @param typeDef - type of the instance
     * @param methodName - method called
     * @throws StatusNotSupportedException - the initial status is invalid for this type
     */
    public void validateInstanceStatus(String         repositoryName,
                                       String         instanceStatusParameterName,
                                       InstanceStatus instanceStatus,
                                       TypeDef        typeDef,
                                       String         methodName) throws StatusNotSupportedException
    {
        if (instanceStatus != null)
        {
            if (typeDef != null)
            {
                List<InstanceStatus>   validStatuses = typeDef.getValidInstanceStatusList();

                for (InstanceStatus validStatus : validStatuses)
                {
                    if (instanceStatus == validStatus)
                    {
                        return;
                    }
                }

                OMRSErrorCode errorCode = OMRSErrorCode.BAD_INSTANCE_STATUS;
                String errorMessage = errorCode.getErrorMessageId()
                                    + errorCode.getFormattedErrorMessage(instanceStatus.getStatusName(),
                                                                         instanceStatusParameterName,
                                                                         methodName,
                                                                         repositoryName,
                                                                         typeDef.getName());

                throw new StatusNotSupportedException(errorCode.getHTTPErrorCode(),
                                                      this.getClass().getName(),
                                                      methodName,
                                                      errorMessage,
                                                      errorCode.getSystemAction(),
                                                      errorCode.getUserAction());
            }
            else
            {
                OMRSErrorCode errorCode = OMRSErrorCode.NULL_TYPEDEF;
                String errorMessage = errorCode.getErrorMessageId()
                                    + errorCode.getFormattedErrorMessage("typeDef",
                                                                         methodName,
                                                                         repositoryName);

                throw new StatusNotSupportedException(errorCode.getHTTPErrorCode(),
                                                      this.getClass().getName(),
                                                      methodName,
                                                      errorMessage,
                                                      errorCode.getSystemAction(),
                                                      errorCode.getUserAction());
            }
        }
    }


    /**
     * Validates an instance status where null is not allowed.
     *
     * @param repositoryName - name of the repository making the request
     * @param instanceStatusParameterName - name of the initial status parameter
     * @param instanceStatus - initial status value
     * @param typeDef - type of the instance
     * @param methodName - method called
     * @throws StatusNotSupportedException - the initial status is invalid for this type
     */
    public void validateNewStatus(String         repositoryName,
                                  String         instanceStatusParameterName,
                                  InstanceStatus instanceStatus,
                                  TypeDef        typeDef,
                                  String         methodName) throws StatusNotSupportedException,
                                                                    InvalidParameterException
    {
        if (instanceStatus != null)
        {
           this.validateInstanceStatus(repositoryName, instanceStatusParameterName, instanceStatus, typeDef, methodName);
        }
        else
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NULL_INSTANCE_STATUS;
            String errorMessage = errorCode.getErrorMessageId()
                                + errorCode.getFormattedErrorMessage(instanceStatusParameterName,
                                                                     methodName,
                                                                     repositoryName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Verify that an instance is not already deleted since the repository is processing a delete request
     * and it does not want to look stupid.
     *
     * @param repositoryName - name of the repository making the request
     * @param instance - instance about to be deleted
     * @param methodName - name of method called
     * @throws InvalidParameterException - the instance is already deleted
     */
    public void validateInstanceStatusForDelete(String         repositoryName,
                                                InstanceHeader instance,
                                                String         methodName) throws InvalidParameterException
    {
        if (instance != null)
        {
            if (instance.getStatus() == InstanceStatus.DELETED)
            {
                /*
                 * Instance is already deleted
                 */
                OMRSErrorCode errorCode = OMRSErrorCode.INSTANCE_ALREADY_DELETED;
                String errorMessage = errorCode.getErrorMessageId()
                                    + errorCode.getFormattedErrorMessage(methodName,
                                                                         repositoryName,
                                                                         instance.getGUID());

                throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                    this.getClass().getName(),
                                                    methodName,
                                                    errorMessage,
                                                    errorCode.getSystemAction(),
                                                    errorCode.getUserAction());
            }
        }
        else
        {
            /*
             * Logic error as the instance should be valid
             */
            final String   thisMethodName = "validateInstanceStatusForDelete";

            throwValidatorLogicError(repositoryName, methodName, thisMethodName);
        }
    }


    public void validateEntityIsDeleted(String         repositoryName,
                                        InstanceHeader instance,
                                        String         methodName) throws EntityNotDeletedException
    {
        if (instance != null)
        {
            if (instance.getStatus() == InstanceStatus.DELETED)
            {
                /*
                 * Instance is already deleted
                 */
                OMRSErrorCode errorCode = OMRSErrorCode.INSTANCE_NOT_DELETED;
                String errorMessage = errorCode.getErrorMessageId()
                        + errorCode.getFormattedErrorMessage(methodName,
                                                             repositoryName,
                                                             instance.getGUID());

                throw new EntityNotDeletedException(errorCode.getHTTPErrorCode(),
                                                    this.getClass().getName(),
                                                    methodName,
                                                    errorMessage,
                                                    errorCode.getSystemAction(),
                                                    errorCode.getUserAction());
            }
        }
        else
        {
            /*
             * Logic error as the instance should be valid
             */
            final String   thisMethodName = "validateEntityIsDeleted";

            throwValidatorLogicError(repositoryName, methodName, thisMethodName);
        }
    }


    public void validateRelationshipIsDeleted(String         repositoryName,
                                              InstanceHeader instance,
                                              String         methodName) throws RelationshipNotDeletedException
    {
        if (instance != null)
        {
            if (instance.getStatus() != InstanceStatus.DELETED)
            {
                /*
                 * Instance is already deleted
                 */
                OMRSErrorCode errorCode = OMRSErrorCode.INSTANCE_NOT_DELETED;
                String errorMessage = errorCode.getErrorMessageId()
                                    + errorCode.getFormattedErrorMessage(methodName,
                                                                         repositoryName,
                                                                         instance.getGUID());

                throw new RelationshipNotDeletedException(errorCode.getHTTPErrorCode(),
                                                          this.getClass().getName(),
                                                          methodName,
                                                          errorMessage,
                                                          errorCode.getSystemAction(),
                                                          errorCode.getUserAction());
            }
        }
        else
        {
            /*
             * Logic error as the instance should be valid
             */
            final String   thisMethodName = "validateRelationshipIsDeleted";

            throwValidatorLogicError(repositoryName, methodName, thisMethodName);
        }
    }


    /**
     * Validate that the types of the two ends of a relationship match the relationship's TypeDef.
     *
     * @param repositoryName - name of the repository making the request
     * @param entityOneProxy - content of end one
     * @param entityTwoProxy - content of end two
     * @param typeDef - typeDef for the relationship
     * @param methodName - name of the method making the request
     * @throws InvalidParameterException - types do not align
     */
    public void validateRelationshipEnds(String        repositoryName,
                                         EntityProxy   entityOneProxy,
                                         EntityProxy   entityTwoProxy,
                                         TypeDef       typeDef,
                                         String        methodName) throws InvalidParameterException
    {
        final String thisMethodName = "validateRelationshipEnds";

        if ((entityOneProxy != null) && (entityTwoProxy != null) && (typeDef != null))
        {
            try
            {
                RelationshipDef    relationshipDef      = (RelationshipDef) typeDef;
                RelationshipEndDef entityOneEndDef      = null;
                RelationshipEndDef entityTwoEndDef      = null;
                TypeDefLink        entityOneTypeDef     = null;
                TypeDefLink        entityTwoTypeDef     = null;
                String             entityOneTypeDefGUID = null;
                String             entityOneTypeDefName = null;
                String             entityTwoTypeDefGUID = null;
                String             entityTwoTypeDefName = null;
                InstanceType       entityOneType        = null;
                InstanceType       entityTwoType        = null;
                String             entityOneTypeGUID    = null;
                String             entityOneTypeName    = null;
                String             entityTwoTypeGUID    = null;
                String             entityTwoTypeName    = null;


                if (relationshipDef != null)
                {
                    entityOneEndDef = relationshipDef.getEndDef1();
                    entityTwoEndDef = relationshipDef.getEndDef2();
                }

                if ((entityOneEndDef != null) && (entityTwoEndDef != null))
                {
                    entityOneTypeDef = entityOneEndDef.getEntityType();
                    entityTwoTypeDef = entityTwoEndDef.getEntityType();
                }

                if ((entityOneTypeDef != null) && (entityTwoTypeDef != null))
                {
                    entityOneTypeDefGUID = entityOneTypeDef.getGUID();
                    entityOneTypeDefName = entityOneTypeDef.getName();
                    entityTwoTypeDefGUID = entityTwoTypeDef.getGUID();
                    entityTwoTypeDefName = entityTwoTypeDef.getName();
                }

                if ((entityOneProxy != null) && (entityTwoProxy != null))
                {
                    entityOneType = entityOneProxy.getType();
                    entityTwoType = entityTwoProxy.getType();
                }

                if ((entityOneType != null) && (entityTwoType != null))
                {
                    entityOneTypeGUID = entityOneType.getTypeDefGUID();
                    entityOneTypeName = entityOneType.getTypeDefName();
                    entityTwoTypeGUID = entityTwoType.getTypeDefGUID();
                    entityTwoTypeName = entityTwoType.getTypeDefName();
                }

                if ((entityOneTypeDefGUID != null) && (entityOneTypeDefName != null) &&
                    (entityTwoTypeDefGUID != null) && (entityTwoTypeDefName != null) &&
                    (entityOneTypeGUID != null)    && (entityOneTypeName != null)    &&
                    (entityTwoTypeGUID != null)    && (entityTwoTypeName != null))
                {
                    if ((entityOneTypeDefGUID.equals(entityOneTypeGUID)) &&
                            (entityTwoTypeDefGUID.equals(entityTwoTypeGUID)) &&
                            (entityOneTypeDefName.equals(entityOneTypeName)) &&
                            (entityTwoTypeDefName.equals(entityTwoTypeName)))
                    {
                        return;
                    }
                }

                OMRSErrorCode errorCode = OMRSErrorCode.INVALID_RELATIONSHIP_ENDS;
                String errorMessage = errorCode.getErrorMessageId()
                                    + errorCode.getFormattedErrorMessage(methodName,
                                                                         repositoryName,
                                                                         typeDef.toString(),
                                                                         entityOneProxy.toString(),
                                                                         entityTwoProxy.toString());

                throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                    this.getClass().getName(),
                                                    methodName,
                                                    errorMessage,
                                                    errorCode.getSystemAction(),
                                                    errorCode.getUserAction());
            }
            catch (InvalidParameterException error)
            {
                throw error;
            }
            catch (Throwable error)
            {
                /*
                 * Logic error as the instance should be valid
                 */
                throwValidatorLogicError(repositoryName, methodName, thisMethodName);
            }
        }
        else
        {
            throwValidatorLogicError(repositoryName,
                                     methodName,
                                     thisMethodName);
        }
    }


    /**
     * Return a boolean indicating whether the supplied entity is classified with one or more of the supplied
     * classifications.
     *
     * @param requiredClassifications - list of required classification - null means that there are no specific
     *                                classification requirements and so results in a true response.
     * @param entity - entity to test.
     * @return boolean result
     */
    public boolean verifyEntityIsClassified(List<String>   requiredClassifications,
                                            EntitySummary  entity)
    {
        if (requiredClassifications != null)
        {
            List<Classification> entityClassifications = entity.getClassifications();

            for (String requiredClassification : requiredClassifications)
            {
                if (requiredClassification != null)
                {
                    for (Classification entityClassification : entityClassifications)
                    {
                        if (entityClassification != null)
                        {
                            if (requiredClassification.equals(entityClassification.getName()))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        else
        {
            return true;
        }

        return false;
    }


    /**
     * Count the number of matching property values that an instance has.  They may come from an entity,
     * classification or relationship.
     *
     * @param matchProperties - the properties to match.
     * @param instanceProperties - the properties from the instance.
     * @return integer count of the matching properties.
     */
    public int countMatchingPropertyValues(InstanceProperties       matchProperties,
                                           InstanceProperties       instanceProperties)
    {
        int       matchingProperties = 0;

        if ((matchProperties != null) && (instanceProperties != null))
        {
            Iterator<String> instancePropertyNames = instanceProperties.getPropertyNames();

            while (instancePropertyNames.hasNext())
            {
                String instancePropertyName = instancePropertyNames.next();

                if (instancePropertyName != null)
                {
                    InstancePropertyValue instancePropertyValue = instanceProperties.getPropertyValue(instancePropertyName);
                    Iterator<String>      matchPropertyNames    = matchProperties.getPropertyNames();

                    while (matchPropertyNames.hasNext())
                    {
                        String matchPropertyName = matchPropertyNames.next();

                        if (matchPropertyName != null)
                        {
                            InstancePropertyValue matchPropertyValue = matchProperties.getPropertyValue(matchPropertyName);

                            if ((instancePropertyName.equals(matchPropertyName)) &&
                                (instancePropertyValue.equals(matchPropertyValue)))
                            {
                                matchingProperties++;
                            }
                        }
                    }
                }
            }
        }

        return matchingProperties;
    }


    /**
     *
     * @param matchProperties - the properties to match.
     * @param instanceProperties - the properties from the instance.
     * @param matchCriteria
     * @return boolean flag indicating whether the two sets of properties match
     */
    public boolean verifyMatchingInstancePropertyValues(InstanceProperties   matchProperties,
                                                        InstanceProperties   instanceProperties,
                                                        MatchCriteria        matchCriteria)
    {
        if (matchProperties != null)
        {
            int matchingProperties = this.countMatchingPropertyValues(matchProperties, instanceProperties);

            switch (matchCriteria)
            {
                case ALL:
                    if (matchingProperties == matchProperties.getPropertyCount())
                    {
                        return true;
                    }
                    break;

                case ANY:
                    if (matchingProperties > 0)
                    {
                        return true;
                    }
                    break;

                case NONE:
                    if (matchingProperties == 0)
                    {
                        return true;
                    }
                    break;
            }
        }
        else
        {
            return true;
        }

        return false;
    }


    /**
     * Validates that an instance has the correct header for it to be a reference copy.
     *
     * @param repositoryName - name of the repository it is to be stored in.
     * @param localMetadataCollectionId  - the unique identifier for the local repository' metadata collection.
     * @param instanceParameterName - the name of the parameter that provided the instance.
     * @param instance - the instance to test
     * @param methodName - the name of the method that supplied the instance.
     * @throws InvalidParameterException - the instance is null or linked to local metadata repository
     */
    public void validateReferenceInstanceHeader(String         repositoryName,
                                                String         localMetadataCollectionId,
                                                String         instanceParameterName,
                                                InstanceHeader instance,
                                                String         methodName) throws InvalidParameterException,
                                                                                  RepositoryErrorException
    {
        if (instance == null)
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NULL_REFERENCE_INSTANCE;
            String errorMessage = errorCode.getErrorMessageId()
                                + errorCode.getFormattedErrorMessage(repositoryName, methodName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        this.validateInstanceType(repositoryName, instance);

        this.validateHomeMetadataGUID(repositoryName, instanceParameterName, instance.getMetadataCollectionId(), methodName);

        if (localMetadataCollectionId.equals(instance.getMetadataCollectionId()))
        {
            OMRSErrorCode errorCode = OMRSErrorCode.LOCAL_REFERENCE_INSTANCE;
            String errorMessage = errorCode.getErrorMessageId()
                                + errorCode.getFormattedErrorMessage(repositoryName, methodName, instanceParameterName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Validates an entity proxy.  It must be a reference copy (ie owned by a different repository).
     *
     * @param repositoryName - name of the repository
     * @param localMetadataCollectionId - unique identifier for this repository's metadata collection
     * @param proxyParameterName - name of the parameter used to provide the parameter
     * @param entityProxy - proxy to add
     * @param methodName - name of the method that adds the proxy
     * @throws InvalidParameterException the entity proxy is null or for an entity homed in this repository
     */
    public void validateEntityProxy (String         repositoryName,
                                     String         localMetadataCollectionId,
                                     String         proxyParameterName,
                                     EntityProxy    entityProxy,
                                     String         methodName) throws InvalidParameterException
    {
        if (entityProxy == null)
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NULL_ENTITY_PROXY;
            String errorMessage = errorCode.getErrorMessageId()
                                + errorCode.getFormattedErrorMessage(repositoryName, proxyParameterName, methodName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        this.validateHomeMetadataGUID(repositoryName, proxyParameterName, entityProxy.getMetadataCollectionId(), methodName);

        if (localMetadataCollectionId.equals(entityProxy.getMetadataCollectionId()))
        {
            OMRSErrorCode errorCode = OMRSErrorCode.LOCAL_ENTITY_PROXY;
            String errorMessage = errorCode.getErrorMessageId()
                                + errorCode.getFormattedErrorMessage(repositoryName, proxyParameterName, methodName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }
    }


    /**
     * Search for property values matching the search criteria (a regular expression)
     *
     * @param repositoryName - name of the repository where the instance came from.
     * @param properties - list of properties associated with the in instance
     * @param searchCriteria - regular expression for testing the property values
     * @param methodName - name of the method requiring the search.
     * @return boolean indicating whether the search criteria is located in any of the string parameter values.
     * @throws RepositoryErrorException - the properties are not properly set up in the instance
     */
    public boolean verifyInstancePropertiesMatchSearchCriteria(String              repositoryName,
                                                               InstanceProperties  properties,
                                                               String              searchCriteria,
                                                               String              methodName) throws RepositoryErrorException
    {
        if (properties == null)
        {
            return false;
        }

        Iterator<String>  propertyNames = properties.getPropertyNames();

        try
        {
            while (propertyNames.hasNext())
            {
                InstancePropertyValue  propertyValue = properties.getPropertyValue(propertyNames.next());

                switch (propertyValue.getInstancePropertyCategory())
                {
                    case PRIMITIVE:
                        PrimitivePropertyValue primitivePropertyValue = (PrimitivePropertyValue)propertyValue;
                        if (primitivePropertyValue.getPrimitiveDefCategory() == PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING)
                        {
                            String   stringProperty = (String)primitivePropertyValue.getPrimitiveValue();

                            if (stringProperty != null)
                            {
                                if (stringProperty.matches(searchCriteria))
                                {
                                    return true;
                                }
                            }
                        }
                        break;

                    case ENUM:
                        EnumPropertyValue enumPropertyValue = (EnumPropertyValue)propertyValue;

                        String  enumValue = enumPropertyValue.getSymbolicName();
                        if (enumValue != null)
                        {
                            if (enumValue.matches(searchCriteria))
                            {
                                return true;
                            }
                        }
                        break;

                    case STRUCT:
                        StructPropertyValue structPropertyValue = (StructPropertyValue)propertyValue;

                        if (verifyInstancePropertiesMatchSearchCriteria(repositoryName,
                                                                        structPropertyValue.getAttributes(),
                                                                        searchCriteria,
                                                                        methodName))
                        {
                            return true;
                        }
                        break;

                    case ARRAY:
                        ArrayPropertyValue arrayPropertyValue = (ArrayPropertyValue)propertyValue;

                        if (verifyInstancePropertiesMatchSearchCriteria(repositoryName,
                                                                        arrayPropertyValue.getArrayValues(),
                                                                        searchCriteria,
                                                                        methodName))
                        {
                            return true;
                        }
                        break;

                    case MAP:
                        MapPropertyValue mapPropertyValue = (MapPropertyValue)propertyValue;

                        if (verifyInstancePropertiesMatchSearchCriteria(repositoryName,
                                                                        mapPropertyValue.getMapValues(),
                                                                        searchCriteria,
                                                                        methodName))
                        {
                            return true;
                        }
                        break;
                }
            }
        }
        catch (Throwable   error)
        {
            /*
             * Probably a class cast error which should never occur.
             */
            OMRSErrorCode errorCode = OMRSErrorCode.BAD_PROPERTY_FOR_INSTANCE;
            String errorMessage = errorCode.getErrorMessageId()
                                + errorCode.getFormattedErrorMessage(repositoryName, methodName);

            throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                                               this.getClass().getName(),
                                               methodName,
                                               errorMessage,
                                               errorCode.getSystemAction(),
                                               errorCode.getUserAction(),
                                               error);
        }

        return false;
    }


    /*
     * ======================
     * Private Methods
     * ======================
     */


    private void validateRepositoryContentManager(String   methodName)
    {
        if (repositoryContentManager == null)
        {
            OMRSErrorCode errorCode = OMRSErrorCode.LOCAL_REPOSITORY_CONFIGURATION_ERROR;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Throws a logic error exception when the repository validator is called with invalid parameters.
     * Normally this means the repository validator methods have been called in the wrong order.
     *
     * @param sourceName - name of the calling repository or service
     * @param originatingMethodName - method that called the repository validator
     * @param localMethodName - local method that detedted the error
     */
    private void throwValidatorLogicError(String     sourceName,
                                          String     originatingMethodName,
                                          String     localMethodName)
    {
        OMRSErrorCode errorCode = OMRSErrorCode.VALIDATION_LOGIC_ERROR;
        String errorMessage     = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                     localMethodName,
                                                                                                     originatingMethodName);

        throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                          this.getClass().getName(),
                                          localMethodName,
                                          errorMessage,
                                          errorCode.getSystemAction(),
                                          errorCode.getUserAction());
    }
}
