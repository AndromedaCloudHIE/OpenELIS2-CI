/**
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
*
* The Original Code is OpenELIS code.
*
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*/
package org.openelisglobal.patient.dao;

import java.util.List;

import org.openelisglobal.common.dao.BaseDAO;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.person.valueholder.Person;

/**
 * @author diane benz
 *
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public interface PatientDAO extends BaseDAO<Patient, String> {

//	public boolean insertData(Patient patient) throws LIMSRuntimeException;

//	public void deleteData(List patients) throws LIMSRuntimeException;

    List<Patient> getAllPatients() throws LIMSRuntimeException;

    List<Patient> getPageOfPatients(int startingRecNo) throws LIMSRuntimeException;

    void getData(Patient patient) throws LIMSRuntimeException;

//	public void updateData(Patient patient) throws LIMSRuntimeException;

    boolean externalIDExists(String patientExternalID);

    Patient readPatient(String idString);

    Patient getPatientByPerson(Person person) throws LIMSRuntimeException;

    Patient getPatientByNationalId(String nationalId);

    Patient getPatientBySubjectNumber(String subjectNumber);

    List<Patient> getPatientsByNationalId(String nationalId) throws LIMSRuntimeException;

    Patient getPatientByExternalId(String externalId);

    List<String> getPatientIdentityBySampleStatusIdAndProject(List<Integer> inclusiveStatusIdList, String study)
            throws LIMSRuntimeException;

    Patient getData(String patientId) throws LIMSRuntimeException;

    List<Patient> getAllMissingFhirUuid();
}
