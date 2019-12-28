function successToast(message, isSticky, time) {
    $().toastmessage('showToast', {
        text     : message,
        sticky   : isSticky,
        position : 'top-right',
        type     : 'success',
        stayTime : time,
        closeText: '',
        close    : function () {
            console.log("toast is closed ...");
        }
    });
}
function errorToast(message, isSticky, time) {
    $().toastmessage('showToast', {
        text     : message,
        sticky   : isSticky,
        position : 'top-right',
        type     : 'error',
        stayTime : time,
        closeText: '',
        close    : function () {
            console.log("toast is closed ...");
        }
    });
}

function noticeToast(message, isSticky, time) {
    $().toastmessage('showToast', {
        text     : message,
        sticky   : isSticky,
        position : 'top-right',
        type     : 'notice',
        stayTime : time,
        closeText: '',
        close    : function () {
            console.log("toast is closed ...");
        }
    });
}