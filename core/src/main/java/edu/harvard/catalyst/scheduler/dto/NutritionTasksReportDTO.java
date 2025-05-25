/**
 * Copyright (c) 2015-2016, President and Fellows of Harvard College
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package edu.harvard.catalyst.scheduler.dto;

import java.util.Date;

public class NutritionTasksReportDTO {
	
    private Integer id;
	private String localId;
	private String visitName;
	private Date scheduledStartTime;
	private Date scheduledEndTime;
	private String subjectLastName;
	private String resource;
	private Boolean anthropometrySimple;
	private Boolean anthropometryComplex;
	private String mealPlanCalculation;
	private String nutrientAnalysis;
	private String educationTime;
	private String questionnaireTime;
	private Boolean vitaport;
    private Boolean assistantTechResearch;
    private Boolean assistantTechMed;
    private Boolean templateNurse;
    private String vitaportStart;
    private String vitaportMonitor;
    private String vitaportDownload;
    private String ecgMonitor;
    private String tempCollection;
    private String scalpElectrode;
    private String vitalSigns;
    private String techMedEcg;
    private String techMedPhlebotomy;
    private String techMedProcessing;
    private String techMedSpecialProcessing;
    private String bloods;
	
	public NutritionTasksReportDTO(){
		
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getVisitName() {
		return visitName;
	}

	public void setVisitName(String visitName) {
		this.visitName = visitName;
	}

	public Date getScheduledStartTime() {
		return scheduledStartTime;
	}

	public void setScheduledStartTime(Date scheduledStartTime) {
		this.scheduledStartTime = scheduledStartTime;
	}

	public Date getScheduledEndTime() {
		return scheduledEndTime;
	}

	public void setScheduledEndTime(Date scheduledEndTime) {
		this.scheduledEndTime = scheduledEndTime;
	}

	public String getSubjectLastName() {
		return subjectLastName;
	}

	public void setSubjectLastName(String subjectLastName) {
		this.subjectLastName = subjectLastName;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Boolean isAnthropometrySimple() {
		return anthropometrySimple;
	}

	public void setAnthropometrySimple(Boolean anthropometrySimple) {
		this.anthropometrySimple = anthropometrySimple;
	}

	public Boolean isAnthropometryComplex() {
		return anthropometryComplex;
	}

	public void setAnthropometryComplex(Boolean anthropometryComplex) {
		this.anthropometryComplex = anthropometryComplex;
	}

	public String getMealPlanCalculation() {
		return mealPlanCalculation;
	}

	public void setMealPlanCalculation(String mealPlanCalculation) {
		this.mealPlanCalculation = mealPlanCalculation;
	}

	public String getNutrientAnalysis() {
		return nutrientAnalysis;
	}

	public void setNutrientAnalysis(String nutrientAnalysis) {
		this.nutrientAnalysis = nutrientAnalysis;
	}

	public String getEducationTime() {
		return educationTime;
	}

	public void setEducationTime(String educationTime) {
		this.educationTime = educationTime;
	}

	public String getQuestionnaireTime() {
		return questionnaireTime;
	}

	public void setQuestionnaireTime(String questionnaireTime) {
		this.questionnaireTime = questionnaireTime;
	}

    public Boolean getVitaport() {
        return vitaport;
    }

    public void setVitaport(Boolean vitaport) {
        this.vitaport = vitaport;
    }

    public Boolean getAssistantTechResearch() {
        return assistantTechResearch;
    }

    public void setAssistantTechResearch(Boolean assistantTechResearch) {
        this.assistantTechResearch = assistantTechResearch;
    }

    public Boolean getAssistantTechMed() {
        return assistantTechMed;
    }

    public void setAssistantTechMed(Boolean assistantTechMed) {
        this.assistantTechMed = assistantTechMed;
    }

    public Boolean getTemplateNurse() {
        return templateNurse;
    }

    public void setTemplateNurse(Boolean templateNurse) {
        this.templateNurse = templateNurse;
    }

    public String getVitaportStart() {
        return vitaportStart;
    }

    public void setVitaportStart(String vitaportStart) {
        this.vitaportStart = vitaportStart;
    }

    public String getVitaportMonitor() {
        return vitaportMonitor;
    }

    public void setVitaportMonitor(String vitaportMonitor) {
        this.vitaportMonitor = vitaportMonitor;
    }

    public String getVitaportDownload() {
        return vitaportDownload;
    }

    public void setVitaportDownload(String vitaportDownload) {
        this.vitaportDownload = vitaportDownload;
    }

    public String getEcgMonitor() {
        return ecgMonitor;
    }

    public void setEcgMonitor(String ecgMonitor) {
        this.ecgMonitor = ecgMonitor;
    }

    public String getTempCollection() {
        return tempCollection;
    }

    public void setTempCollection(String tempCollection) {
        this.tempCollection = tempCollection;
    }

    public String getScalpElectrode() {
        return scalpElectrode;
    }

    public void setScalpElectrode(String scalpElectrode) {
        this.scalpElectrode = scalpElectrode;
    }

    public String getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(String vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public String getTechMedEcg() {
        return techMedEcg;
    }

    public void setTechMedEcg(String techMedEcg) {
        this.techMedEcg = techMedEcg;
    }

    public String getTechMedPhlebotomy() {
        return techMedPhlebotomy;
    }

    public void setTechMedPhlebotomy(String techMedPhlebotomy) {
        this.techMedPhlebotomy = techMedPhlebotomy;
    }

    public String getTechMedProcessing() {
        return techMedProcessing;
    }

    public void setTechMedProcessing(String techMedProcessing) {
        this.techMedProcessing = techMedProcessing;
    }

    public String getTechMedSpecialProcessing() {
        return techMedSpecialProcessing;
    }

    public void setTechMedSpecialProcessing(String techMedSpecialProcessing) {
        this.techMedSpecialProcessing = techMedSpecialProcessing;
    }

    public String getBloods() {
        return bloods;
    }

    public void setBloods(String bloods) {
        this.bloods = bloods;
    }
}

	
	