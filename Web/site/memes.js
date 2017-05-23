var xhr;
var _orgAjax = jQuery.ajaxSettings.xhr;
jQuery.ajaxSettings.xhr = function () {
  xhr = _orgAjax();
  return xhr;
};

function follow(button, id, num=null, numStr=null) {
	// follows the user with $id, expects:
	// button 	to be the html button that triggered this event
	// id 		to be the user's id
	// (num)	to be the html element storing the number of followers
	// (numStr)	to be the html element storing the associated text

	$.post('/ajax/follow.php', {id: id}, function (data) {
		if(data.success && (typeof data.followed !== 'undefined')) {
			$(button).html(data.followed ? 'Unfollow' : 'Follow');
			$(button).blur();
			if(num!=null) $(num).html(data['followers-num']);
			if(numStr!=null) $(numStr).html(data['followers-str']);
		}
		console.log('follow()',data);
	}, 'json').fail(function () {
		if(xhr.responseURL.search("goingto=")>-1) 
			// user not logged in (probably)
			expandFooter();
	});
}

function star(button, id, num=null, numStr=null) {
	// stars the meme with $id, expects:
	// button 	to be the icon button that triggered this event
	// id 		to be the meme's id
	// (num)	to be the html element storing the number of stars
	// (numStr)	to be the html element storing the associated text

	$.post('/ajax/star.php', {id: id}, function (data) {
		if(data.success && (typeof data.starred !== 'undefined')) {
			if(data.starred == 1) $(button).addClass('icon-star-full').removeClass('icon-star-empty');
			else $(button).addClass('icon-star-empty').removeClass('icon-star-full');
			if(num!=null) $(num).html(data['stars-num']);
			if(numStr!=null) $(numStr).html(data['stars-str']);
		}
		console.log('like()',data);
	}, 'json').fail(function () {
		if(xhr.responseURL.search("goingto=")>-1) 
			// user not logged in (probably)
			expandFooter();
	});
}

function comment(button, id, comment, comments=null, num=null, numStr=null) {
	// stars the meme with $id, expects:
	// button 	to be the icon button that triggered this event
	// id 		to be the meme's id
	// comment	to be the input box 
	//(comments)to be the comments section
	// (num)	to be the html element storing the number of comments
	// (numStr)	to be the html element storing the associated text

	$.post('/ajax/comment.php', {id: id, comment: $(comment).val()}, function (data) {
		if(data.success) {
			$(button).blur();
			if(comments!=null) // simply pop that comment in the dom
				$(comments).append("<div class='meme-comment'><h4 class='meme-comment-name'><div class='meme-ago'>0s</div><a href='"+data['commenter']['link']+"' title='"+data['commenter']['name']+"'><img src='"+data['commenter']['pic']+"' class='pp' alt='"+data['commenter']['username']+" profile picture' /> "+data['commenter']['username']+"</a></h4><p>"+htmlEncode($(comment).val())+"</p></div>");
			$(comment).val('');
			if(num!=null) $(num).html(data['comments-num']);
			if(numStr!=null) $(numStr).html(data['comments-str']);
		}
		console.log('comment()',data);
	}, 'json').fail(function () {
		if(xhr.responseURL.search("goingto=")>-1) 
			// user not logged in (probably)
			expandFooter();
	});
}

function repost(id, caption) {
	// stars the meme with $id, expects:
	// id 		to be the meme's id
	// caption	to be the caption input box

	$.post('/ajax/repost.php', {id: id, caption: $(caption).val()}, function (data) {
		if(data.success) window.location.href = data.link;
		console.log('repost()',data);
	}, 'json').fail(function () {
		if(xhr.responseURL.search("goingto=")>-1) 
			// user not logged in (probably)
			expandFooter();
	});
}


function expand(elem) {
	// expand them [elem]ent
	$(elem).slideDown();
}

function expandFooter() {
	//expand the footer
	$('footer .wrapper.signup').animate({paddingTop: "20px",paddingBottom: "20px"},100,'swing',function() {
		$(this).animate({paddingTop: "0px",paddingBottom: "0px"},1500,'swing')
	});
}

function htmlEncode(s) { // adapted from http://stackoverflow.com/a/784698
	var el = document.createElement("div");
	el.innerText = el.textContent = s;
	return el.innerHTML;
}


// load more on scroll

var page = 0; // the current page we're on
var updating = false; // whether it's currently updating

$(window).scroll(function() {
	if(updating)
		return;

	var url = '/ajax/more';

	var container = 'body > .wrapper'; // container holding the memes

	var id = 0;

	if(typeof profile !== 'undefined') {
		// you can't easily url match these and I don't want to make the default 
		id = profile;
		url += 'profile.php';
		container += ' .memeGrid';
	}
	else { // do some url matching because that seems like a totally reliable way of doing it...
		switch(window.location.pathname) {
			case '/':
			case '/index.php':
				url += 'feed.php';
				break;


			case '/hot':
			case '/hot/':
			case '/hot/index.php':
				url += 'hot.php';
				break;

			case '/starred':
			case '/starred/':
			case '/starred/index.php':
				url += 'starred.php';
				container += ' .memeGrid';
				break;

			case '/notifications':
			case '/notifications/':
			case '/notifications/index.php':
				url += 'notifications.php';
				container += ' .notifications-containter';
				break;

			default:
				return;
		}
	}

	var wheight = $(window).height();
	var dheight = $(document).height();
	var scroll = $(document).scrollTop();

	if(scroll > (dheight - 2 * wheight)) {
		console.log('loading more');
		updating = true;

		var updateText = $('<h3 class="center">Updating&hellip;</h3>').appendTo(container);

		$.post(url, {page: ++page, profile: id}, function (data) {
			updateText.remove();
			if(data == '') return;

			if(xhr.responseURL.search("goingto=")>-1)
				return 
			
			$(container).append(data);

			updating = false;
		});
	}

	//console.log('height: '+height+' scroll: '+scroll);
});
