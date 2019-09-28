$(function() {
    $( ".navbar-nav .nav-item").click(function() {
        window.location.href = "../mainScreen/mainScreen.html";
    });
    $("#create-branch-btn").click(createBranch);
    $('#create-branch-form').submit(function (e) {
        e.preventDefault();
        var id = window.location.href.split('=')[1];
        $.ajax({
            type: $(this).attr('method'),
            url: $(this).attr('action'),
            data: {branchName: $("#branch-name",this).val(), id: id},
            timeout: 2000,
            error: function (a) {
                $('.modal-body-error').text(a.responseText);
                $('#error-modal').modal('show');

            },
            success: function(a) {
                createBranchView($.parseJSON(a));
                $('#create-branch-modal').modal('hide');
            }
        })

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
                "<div class='row main-container'></div>"
            );
            $(".row-title").append(
                "<div class='card card-repo' style='width: 50rem;background: rgba(202,255,240,0.74);'>" +
                "<div class='card-body'>" +
                "<h4 class='card-title'>" + repositoryDetails.Repository.name + "</h4>" +
                "<h6 class='card-subtitle mb-2 text-muted branches-count'>Number of Branches: " + $(".card-branch").length + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted head-title'>Head Branch: " + repositoryDetails.Repository.activeBranch + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Date: " + repositoryDetails.Repository.commitDate + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Message: " + repositoryDetails.Repository.commitMessage + "</h6>" +
                "</div>" +
                "</div>");
            delete repositoryDetails.Repository;
            for (var k in repositoryDetails) {
                $(".main-container").append(
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
                $(".branches-count").text("Number of Branches: " + $(".card-branch").length);
            }
            $(".delete-btn").click(deleteBranch);
            $(".head-btn").click(changeHead);
        }
    });
}

function createBranchView(branchDetails) {
    $(".main-container").append(
        "<div class='card card-branch' style='width: 50rem;background: rgba(255,196,157,0.74);'>" +
        "<div class='card-body'>" +
        "<h4 class='card-title'>Branch Name: " + branchDetails.Name + "</h4>" +
        "<h6 class='card-subtitle mb-2 text-muted'>Pointing Commit: " + branchDetails.Commit+ "</h6>" +
        "<h6 class='card-subtitle mb-2 text-muted'>Is Tracking: " + branchDetails.IsTracking + "</h6>" +
        "<h6 class='card-subtitle mb-2 text-muted'>Is Remote: " + branchDetails.IsRemote + "</h6>" +
        "<h6 class='card-subtitle mb-2 text-muted'>Tracking After: " + branchDetails.TrackingAfter + "</h6>" +
        "</div>" +
        "<div class='buttons-column col-lg-4'>" +
        "<button type='button' class='btn btn-branch delete-btn btn-danger'>Delete Branch</button>" +
        "<div class='divider'></div>" +
        "<button type='button' class='btn btn-branch head-btn btn-info'>Checkout as Head</button>" +
        "</div>" +
        "</div>");
    $(".card-branch").last().attr('name', branchDetails.Name);
    $(".delete-btn").last().click(deleteBranch);
    $(".head-btn").last().click(changeHead);
    $(".branches-count").text("Number of Branches: " + $(".card-branch").length);
}

function deleteBranch() {
    var deleteBranchUrl = buildUrlWithContextPath("deleteBranch");
    var id = window.location.href.split('=')[1];
    var branch = $(this);
    var branchName = $(this).parent().parent().attr('name');
    $.ajax( {
        data: {
            name: branchName,
            id: id
        },
        timeout: 2000,
        method: 'POST',
        url: deleteBranchUrl,
        error : function (a) {
            $('#error-modal').modal('show');
            $('.modal-body-error').text(a.responseText);
        },
        success: function() {
            branch.parent().parent().remove();
            $(".branches-count").text("Number of Branches: " + $(".card-branch").length);
        }
    })
}

function changeHead() {
    var id = window.location.href.split('=')[1];
    var checkoutUrl = buildUrlWithContextPath("checkout");
    var branchName = $(this).parent().parent().attr('name');
    $.ajax( {
        data: {
            name: branchName,
            id: id
        },
        method: 'POST',
        url: checkoutUrl,
        error : function (a) {
            if (a.responseText.includes("checkout into a remote branch")) {
                //$('.modal-body-error').text("You are trying to checkout into a remote branch, this operation is forbidden." +
                   // " Please checkout by using a remote tracking branch instead.");
                errorToast("You are trying to checkout into a remote branch, this operation is forbidden." +
                    " Please checkout by using a remote tracking branch instead.",true);
            }
            else {
               // $('#error-modal').modal('show');
                //$('.modal-body-error').text(a.responseText);
                errorToast(a.responseText,true)
            }
           // $('#error-modal').modal('show');
        },
        success: function() {
            $(".head-title").text("Head Branch: " + branchName);

        }
    })
}

function createBranch() {
    $('#create-branch-modal').modal('show');
}