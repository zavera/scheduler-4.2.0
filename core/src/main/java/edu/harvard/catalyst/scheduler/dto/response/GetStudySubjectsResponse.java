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
package edu.harvard.catalyst.scheduler.dto.response;

import edu.harvard.catalyst.scheduler.entity.State;
import edu.harvard.catalyst.scheduler.entity.Study;
import edu.harvard.catalyst.scheduler.entity.StudySubject;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.util.MiscUtil;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static edu.harvard.catalyst.hccrc.core.util.RichList.enrich;
import static edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException.logAndThrow;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 4/7/14
 * Time: 3:27 PM
 */
public final class GetStudySubjectsResponse {

    private final Long totalCount;

    final List<StudySubject1> studySubject1s;

    private GetStudySubjectsResponse(final List<StudySubject1> studySubject1s, final Long totalCount) {
        this.totalCount = totalCount;
        this.studySubject1s = studySubject1s;
    }

    static final class StudySubject1 {
        private final Integer id;
        private final Integer subjectMrnId;
        private final Integer studyId;
        private final Integer subjectId;

        private final String subjectMRN;
        private final String subjectMRNInstitution;
        private final String subjectLastName;
        private final String subjectFirstName;
        private final Date subjectDOB;
        private final String subjectCity;
        private final String subjectState;
        private final String subjectPrimaryContact;
        private final boolean subjectStatus;
        private final String subjectSchedulerGender;

        StudySubject1(final StudySubject studySubject) {
            if (studySubject == null) {
                logAndThrow("StudySubject should not be null!");
            }

            final Study study = studySubject.getStudy();
            final Subject subject = studySubject.getSubject();

            if (study == null) {
                logAndThrow("Study should not be null!");
            }

            if (subject == null) {
                logAndThrow("Subject should not be null!");
            }
            if (studySubject.getSubjectMrn() == null) {
                logAndThrow("StudySubject's subjectMrn should not be null!");
            }
            id = studySubject.getId();
            subjectMrnId = studySubject.getSubjectMrn().getId();

            studyId = study.getId();
            subjectId = subject.getId();

            subjectMRN = SubjectDataEncryptor.decrypt(studySubject.getSubjectMrn().getMrn());
            if(studySubject.getSubjectMrn().getSite() != null)
            {
                subjectMRNInstitution = studySubject.getSubjectMrn().getSite();
            }
            else
            {
                subjectMRNInstitution = "";
            }

            subjectLastName = subject.getLastName();
            subjectFirstName = subject.getFirstName();
            subjectDOB = subject.getBirthdate();

            String city = subject.getCity();
            if(null == city)
            {
                city = "";
            }
            subjectCity = city;

            State state = subject.getState();
            if (null != state) {
                subjectState = state.getName();
            } else {
                subjectState = "";
            }

            String primaryContact = subject.getPrimaryContactNumber();
            if(null == primaryContact)
            {
                primaryContact = "";
            }
            subjectPrimaryContact = primaryContact;
            subjectStatus = studySubject.getActive();

            subjectSchedulerGender = studySubject.getSubject().getGender().getName();
        }
    }

    public static GetStudySubjectsResponse createGetStudiesSubjectsResponse(
            final List<StudySubject> studySubjects,
            final Long total) {
        final List<StudySubject1> studySubject1List = enrich(studySubjects).map(ss -> new StudySubject1(ss)).toList();

        return new GetStudySubjectsResponse(studySubject1List, total);
    }

    public static final Comparator<StudySubject> StudySubjectLastNameComparatorDesc = invert(ignoreCaseAndCompareVia(Subject::getLastName));

    public static final Comparator<StudySubject> StudySubjectMRNComparatorDesc = invert(compareViaMrn());

    public static final Comparator<StudySubject> StudySubjectFirstNameComparatorDesc = invert(ignoreCaseAndCompareVia(Subject::getFirstName));

    public static final Comparator<StudySubject> StudySubjectContactComparatorDesc = invert(compareViaContactNumber(Subject::getPrimaryContactNumber));

    public static final Comparator<StudySubject> StudySubjectBirthdateComparatorDesc = invert(compareVia(Subject::getBirthdate));

    public static final Comparator<StudySubject> StudySubjectCityComparatorDesc = invert(ignoreCaseAndCompareVia(Subject::getCity));

    public static final Comparator<StudySubject> StudySubjectStateComparatorDesc = invert(compareVia(Subject::getState));

    public static final Comparator<StudySubject> StudySubjectLastNameComparatorAsc = ignoreCaseAndCompareVia(Subject::getLastName);

    public static final Comparator<StudySubject> StudySubjectMRNComparatorAsc = compareViaMrn();

    public static final Comparator<StudySubject> StudySubjectFirstNameComparatorAsc = ignoreCaseAndCompareVia(Subject::getFirstName);

    public static final Comparator<StudySubject> StudySubjectContactComparatorAsc = compareViaContactNumber(Subject::getPrimaryContactNumber);

    public static final Comparator<StudySubject> StudySubjectBirthdateComparatorAsc = compareVia(Subject::getBirthdate);

    public static final Comparator<StudySubject> StudySubjectCityComparatorAsc = ignoreCaseAndCompareVia(Subject::getCity);

    public static final Comparator<StudySubject> StudySubjectStateComparatorAsc = compareVia(Subject::getState);


    private static final Comparator<StudySubject> compareViaContactNumber(final Function<Subject, String> field) {
        return (o1, o2) -> {
            String field1 = field.apply(o1.getSubject());
            String field2 = field.apply(o2.getSubject());

            if (field1 == null && field2 == null)
                return 0;
            if (field1 == null)
                return -1;
            if (field2 == null)
                return 1;

            field1 = MiscUtil.dbFormatPhoneNumber(field1, false, false);
            field2 = MiscUtil.dbFormatPhoneNumber(field2, false, false);

            return field1.compareTo(field2);
        };
    }

    private static final Comparator<StudySubject> compareViaMrn() {
        return (o1, o2) -> {

            String mrn1 = o1.getDecryptedMrn();
            String mrn2 = o2.getDecryptedMrn();

            //try to convert both to numbers; if they both convert, great; if not; then compare strings
            try {
                int mrnInt1 = Integer.parseInt(mrn1);
                int mrnInt2 = Integer.parseInt(mrn2);

                return Integer.compare(mrnInt1,mrnInt2);
            } catch (NumberFormatException e){
                //parsing didn't work; do a string compare instead
                return mrn1.compareTo(mrn2);
            }
        };
    }
    private static final <F extends Comparable<F>> Comparator<StudySubject> compareVia(final Function<Subject, F> field) {
        return (o1, o2) -> {
            final F field1 = field.apply(o1.getSubject());
            final F field2 = field.apply(o2.getSubject());

            if (field1 == null && field2 == null)
                return 0;
            if (field1 == null)
                return -1;
            if (field2 == null)
                return 1;

            return field1.compareTo(field2);
        };
    }
    
    private static final Comparator<StudySubject> ignoreCaseAndCompareVia(final Function<Subject, String> field) {
        return (o1, o2) -> {
            final String field1 = field.apply(o1.getSubject());
            final String field2 = field.apply(o2.getSubject());

            if (field1 == null && field2 == null)
                return 0;
            if (field1 == null)
                return -1;
            if (field2 == null)
                return 1;

            return field1.compareToIgnoreCase(field2);
        };
    }
    
    private static final Comparator<StudySubject> invert(final Comparator<StudySubject> c) {
        return (o1, o2) -> c.compare(o2, o1);
    }
}
