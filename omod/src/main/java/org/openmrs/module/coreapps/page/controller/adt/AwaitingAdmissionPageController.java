package org.openmrs.module.coreapps.page.controller.adt;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.converter.MapElementConverter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetUtil;
import org.openmrs.module.reporting.dataset.definition.VisitDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Collections;
import java.util.List;

public class AwaitingAdmissionPageController {
    private final Log log = LogFactory.getLog(getClass());

    public void get(PageModel model,
                    @SpringBean AllDefinitionLibraries libraries,
                    @SpringBean DataSetDefinitionService dsdService,
                    @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) throws EvaluationException {

        EvaluationContext context = new EvaluationContext();
        List<Extension> admissionActions = appFrameworkService.getAllEnabledExtensions("coreapps.app.awaitingAdmissionActions");
        Collections.sort(admissionActions);
        model.addAttribute("admissionActions", admissionActions);

        DataSet dataSet = null;

        VisitDataSetDefinition dsd = new VisitDataSetDefinition();
        AwaitingAdmissionVisitQuery query = new AwaitingAdmissionVisitQuery();
        dsd.addRowFilter(query, null);

        dsd.addColumn("visitId",
                libraries.getDefinition(VisitDataDefinition.class, "reporting.library.visitDataDefinition.builtIn.visitId"), "" );

        dsd.addColumn("patientId",
                libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.patientId"), "");

        dsd.addColumn("patientLastName",
                libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.familyName"), "");

        dsd.addColumn("patientFirstName",
                libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.givenName"), "");

        dsd.addColumn("primaryIdentifier",
                libraries.getDefinition(PatientDataDefinition.class, "emrapi.patientDataDefinition.primaryIdentifier"),
                "", new PropertyConverter(String.class, "identifier"));

        dsd.addColumn("mostRecentAdmissionRequestFromLocation",
                libraries.getDefinition(VisitDataDefinition.class, "emrapi.visitDataDefinition.mostRecentAdmissionRequest"),
                "", new MapElementConverter("fromLocation", null));

        dsd.addColumn("mostRecentAdmissionRequestToLocation",
                libraries.getDefinition(VisitDataDefinition.class, "emrapi.visitDataDefinition.mostRecentAdmissionRequest"),
                "", new MapElementConverter("toLocation", null));

        dsd.addColumn("mostRecentAdmissionRequestDatetime",
                libraries.getDefinition(VisitDataDefinition.class, "emrapi.visitDataDefinition.mostRecentAdmissionRequest"),
                "", new MapElementConverter("datetime", null));

        dsd.addColumn("mostRecentAdmissionRequestProvider",
                libraries.getDefinition(VisitDataDefinition.class, "emrapi.visitDataDefinition.mostRecentAdmissionRequest"),
                "", new MapElementConverter("provider", null));

        dsd.addColumn("mostRecentAdmissionRequestDiagnoses",
                libraries.getDefinition(VisitDataDefinition.class, "emrapi.visitDataDefinition.mostRecentAdmissionRequest"),
                "", new MapElementConverter("diagnoses", null));


        // add the paper record identifier, if the definition is available (provided by the paper record module)
        PatientDataDefinition paperRecordIdentifierDefinition =  libraries.getDefinition(PatientDataDefinition.class, "paperrecord.patientDataDefinition.paperRecordIdentifier");
        if (paperRecordIdentifierDefinition != null) {
            model.addAttribute("paperRecordIdentifierDefinitionAvailable", true);
            dsd.addColumn("paperRecordIdentifier", paperRecordIdentifierDefinition, "",
                    new PropertyConverter(String.class, "identifier"));
        }
        else {
            model.addAttribute("paperRecordIdentifierDefinitionAvailable", false);
        }

        dsd.addSortCriteria("mostRecentAdmissionRequestDatetime", SortCriteria.SortDirection.ASC);

        dataSet = dsdService.evaluate(dsd, context);
        model.addAttribute("awaitingAdmissionList", DataSetUtil.simplify(dataSet));
    }


}
