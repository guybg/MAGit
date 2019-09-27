$(function() {
    $( ".navbar-nav .nav-item" ).click(function() {
        window.location.href = "../mainScreen/mainScreen.html";
    });
    getRepositoryDetails();
});
var numOfBranches;

function getRepositoryDetails() {
    var repositoryDetails;
    var id = window.location.href.split('=')[1];
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
            numOfBranches = repositoryDetails.Repository.branchesNum;
            $(".details-container").append(
                "<div class='row-branches-info'></div>"
            );
            $(".row-title").append(
                "<div class='card card-repo' style='width: 50rem;background: rgba(202,255,240,0.74);'>" +
                "<div class='card-body'>" +
                "<h4 class='card-title'>" + repositoryDetails.Repository.name + "</h4>" +
                "<h6 class='card-subtitle mb-2 text-muted branches-count'>Number of Branches: " + repositoryDetails.Repository.branchesNum + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted head-title'>Head Branch: " + repositoryDetails.Repository.activeBranch + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Date: " + repositoryDetails.Repository.commitDate + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Message: " + repositoryDetails.Repository.commitMessage + "</h6>" +
                "</div>" +
                "</div>");
            delete repositoryDetails.Repository;
            for (var k in repositoryDetails) {
                $(".row-branches-info").append(
                    "<div class='card card-branch' style='width: 50rem;background: rgba(255,196,157,0.74);'>" +
                    "<div class='card-body'>" +
                    "<h4 class='card-title'>Branch Name: " + k + "</h4>" +
                    "<h6 class='card-subtitle mb-2 text-muted'>Pointing Commit: " + repositoryDetails[k].Commit+ "</h6>" +
                    "<h6 class='card-subtitle mb-2 text-muted'>Is Tracking: " + repositoryDetails[k].IsTracking + "</h6>" +
                    "<h6 class='card-subtitle mb-2 text-muted'>Is Remote: " + repositoryDetails[k].IsRemote + "</h6>" +
                    "<h6 class='card-subtitle mb-2 text-muted'>Tracking After: " + repositoryDetails[k].TrackingAfter + "</h6>" +
                    "</div>" +
                    "<div class='buttons-column col-lg-4'>" +
                    "<button type='button' class='btn btn-branch delete-btn btn-danger'>Delete Branch</button>" +
                    "<div class='divider'></div>" +
                    "<button type='button' class='btn btn-branch head-btn btn-info'>Checkout as Head</button>" +
                    "</div>" +
                    "</div>");
                $(".card-branch").last().attr('name', k);
            }
            $(".delete-btn").click(deleteBranch);
            $(".head-btn").click(changeHead);
        }
    });

}

function deleteBranch() {
    var deleteBranchUrl = buildUrlWithContextPath("deleteBranch");
    var branch = $(this);
    var branchName = $(this).parent().parent().attr('name');
    $.ajax( {
        data: {
            "name": branchName
        },
        method: 'POST',
        url: deleteBranchUrl,
        error : function (a) {
            $('#modal').modal('show');
            $('.modal-body').text(a.responseText);
        },
        success: function() {
            branch.parent().parent().remove();
            $(".branches-count").text("Number of Branches: " + (--numOfBranches));
        }
    })
}

function changeHead() {
    var checkoutUrl = buildUrlWithContextPath("checkout");
    var branchName = $(this).parent().parent().attr('name');
    $.ajax( {
        data: {
            "name": branchName
        },
        method: 'POST',
        url: checkoutUrl,
        error : function (a) {
            $('#modal').modal('show');
            $('.modal-body').text(a.responseText);
        },
        success: function() {
            $(".head-title").text("Head Branch: " + branchName);
        }
    })
}