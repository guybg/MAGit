function getRepositoryDetails() {
    var id = window.location.href.split('=')[1];
    var repositoryDetails;
    var REPO_DETAILS_URL = buildUrlWithContextPath("repodetails");
    $.ajax( {
        type: 'GET',
        data: {
            'id' : id
        },
        url: REPO_DETAILS_URL,
        timeout: 2000,
        error : function () {},
        success: function (a) {
            //"{branchesNum=2, commitMessage=changed Foo PSVM to say hello to tao tao, activeBranch=master, name=rep 1, commitDate=Sun Jun 09 20:25:10 IDT 2019}{"HEAD":{"mBranchName":"master","mPointedCommitSha1":{"mSha1Code":"9e10ad75f3f2b5eea8ab9ba42263e742239ffc4e"},"mIsRemote":false,"mTracking":false},"test":{"mBranchName":"test","mPointedCommitSha1":{"mSha1Code":"013855ca533c572d3a29940f08048aa1ea8823ff"},"mIsRemote":false,"mTracking":false},"master":{"mBranchName":"master","mPointedCommitSha1":{"mSha1Code":"9e10ad75f3f2b5eea8ab9ba42263e742239ffc4e"},"mIsRemote":false,"mTracking":false}}"
            repositoryDetails = a;
            $(".row-title").append(
                "<div class='card' style='width: 50rem;background: rgba(202,255,240,0.74);'>" +
                "<div class='card-body'>" +
                "<h4 class='card-title'>" + repositoryDetails.name + "</h4>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Number of Branches: " + repositoryDetails.branchesNum + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Active Branch: " + repositoryDetails.activeBranch + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Date: " + repositoryDetails.commitDate + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Message: " + repositoryDetails.commitMessage + "</h6>" +
                "</div>" +
                "</div>");
        }
    });


}

$(function() {
    getRepositoryDetails();
});
