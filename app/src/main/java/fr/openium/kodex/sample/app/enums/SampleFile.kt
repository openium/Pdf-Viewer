package fr.openium.kodex.sample.app.enums

enum class DocumentResource(val localFileName: String, val downloadUrl: String) {
    SAT_PRACTICE_TEST(
        localFileName = "sat-practice-test.pdf",
        downloadUrl = "https://satsuite.collegeboard.org/media/pdf/sat-practice-test-1-digital.pdf"
    ),
    TOEIC_SAMPLE_TEST(
        localFileName = "toeic-sample-test.pdf",
        downloadUrl = "https://www.ets.org/pdfs/toeic/toeic-listening-reading-sample-test.pdf"
    ),
    GENERIC_TEST_DOCUMENT(
        localFileName = "generic-test-document.pdf",
        downloadUrl = "https://onlinetestcase.com/wp-content/uploads/2023/06/1.5-MB.pdf"
    ),
    CAMBRIDGE_SAMPLE_PAPERS(
        localFileName = "cambridge-sample-papers.pdf",
        downloadUrl = "https://www.cambridgeenglish.org/Images/young-learners-sample-papers-2018-vol1.pdf"
    )
}