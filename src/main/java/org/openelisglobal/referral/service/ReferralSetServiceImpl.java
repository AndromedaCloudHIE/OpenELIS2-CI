package org.openelisglobal.referral.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.services.IStatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.services.StatusService.OrderStatus;
import org.openelisglobal.dataexchange.fhir.exception.FhirLocalPersistingException;
import org.openelisglobal.note.service.NoteService;
import org.openelisglobal.referral.fhir.service.FhirReferralService;
import org.openelisglobal.referral.fhir.service.TestNotFullyConfiguredException;
import org.openelisglobal.referral.valueholder.Referral;
import org.openelisglobal.referral.valueholder.ReferralResult;
import org.openelisglobal.referral.valueholder.ReferralSet;
import org.openelisglobal.result.service.ResultService;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.sample.service.SampleService;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.spring.util.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferralSetServiceImpl implements ReferralSetService {

    @Autowired
    private ReferralService referralService;
    @Autowired
    private ReferralResultService referralResultService;
    @Autowired
    private ResultService resultService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private FhirReferralService fhirReferralService;

    @Transactional
    @Override
    public void updateReferralSets(List<ReferralSet> referralSetList, List<Sample> modifiedSamples,
            Set<Sample> parentSamples, List<ReferralResult> removableReferralResults, String sysUserId) {
        for (ReferralSet referralSet : referralSetList) {
            referralService.update(referralSet.getReferral());

            for (ReferralResult referralResult : referralSet.getUpdatableReferralResults()) {
                Result rResult = referralResult.getResult();
                if (rResult != null) {
                    if (rResult.getId() == null) {
                        resultService.insert(rResult);
                    } else {
                        rResult.setSysUserId(sysUserId);
                        resultService.update(rResult);
                    }
                }

                if (referralResult.getId() == null) {
                    referralResultService.insert(referralResult);
                } else {
                    referralResultService.update(referralResult);
                }
            }

            if (referralSet.getNote() != null) {
                if (referralSet.getNote().getId() == null) {
                    noteService.insert(referralSet.getNote());
                } else {
                    noteService.update(referralSet.getNote());
                }
            }
        }

        for (ReferralResult referralResult : removableReferralResults) {

            referralResult.setSysUserId(sysUserId);
            referralResultService.delete(referralResult);

            if (referralResult.getResult() != null && referralResult.getResult().getId() != null) {
                referralResult.getResult().setSysUserId(sysUserId);
                resultService.delete(referralResult.getResult());
            }
        }

        setStatusOfParentSamples(modifiedSamples, parentSamples, sysUserId);

        for (Sample sample : modifiedSamples) {
            sampleService.update(sample);
        }

        for (ReferralSet referralSet : referralSetList) {
            if (referralSet.getReferral().isCanceled()) {
//                try {
//                    fhirReferralService.cancelReferralToOrganization(
//                            referralSet.getReferral().getOrganization().getId(),
//                            referralSet.getReferral().getAnalysis().getSampleItem().getSample().getId(),
//                            Arrays.asList(referralSet.getReferral().getAnalysis().getId()));
//                } catch (FhirLocalPersistingException e) {
//                    // TODO don't catch since this is a considerable error in OE world going ahead?
//                    LogEvent.logError(e);
//                }
            } else {
                try {
                    fhirReferralService.referAnalysisesToOrganization(referralSet.getReferral());
                } catch (TestNotFullyConfiguredException e) {
                    LogEvent.logError(this.getClass().getName(), "updateRefreralSets",
                            "unable to automatically refer a test that does not have a loinc code set");
                } catch (FhirLocalPersistingException e) {
                    LogEvent.logError(this.getClass().getName(), "updateRefreralSets",
                            "had a problem saving the referral locally in fhir");
                }
            }
        }

    }

    private void setStatusOfParentSamples(List<Sample> modifiedSamples, Set<Sample> parentSamples, String sysUserId) {
        for (Sample sample : parentSamples) {
            List<Analysis> analysisList = analysisService.getAnalysesBySampleId(sample.getId());

            String finalizedId = SpringContext.getBean(IStatusService.class).getStatusID(AnalysisStatus.Finalized);
            boolean allAnalysisFinished = true;

            if (analysisList != null) {
                for (Analysis childAnalysis : analysisList) {
                    Referral referral = referralService.getReferralByAnalysisId(childAnalysis.getId());
                    List<ReferralResult> referralResultList;

                    if (referral == null || referral.getId() == null) {
                        referralResultList = new ArrayList<>();
                    } else {
                        referralResultList = referralResultService.getReferralResultsForReferral(referral.getId());
                    }

                    if (referralResultList.isEmpty() || referral == null) {
                        if (!finalizedId.equals(childAnalysis.getStatusId())) {
                            allAnalysisFinished = false;
                            break;
                        }
                    } else {
                        for (ReferralResult referralResult : referralResultList) {
                            if (referralResult.getResult() == null
                                    || GenericValidator.isBlankOrNull(referralResult.getResult().getValue())) {
                                if (!(referral.isCanceled() && finalizedId.equals(childAnalysis.getStatusId()))) {
                                    allAnalysisFinished = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (allAnalysisFinished) {
                sample.setStatusId(SpringContext.getBean(IStatusService.class).getStatusID(OrderStatus.Finished));
                sample.setSysUserId(sysUserId);
                modifiedSamples.add(sample);
            }
        }
    }

}
