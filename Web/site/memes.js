function follow(button, id, num=null, numStr=null) {
	// follows the user with $id, expects:
	// button 	to be the html button that triggered this event
	// id 		to be the user's id
	// (num)	to be the html element storing the number of followers
	// (numStr)	to be the html element storing the associated text

	$.post('/ajax/follow.php', {id: id}, function (data) {
		if(data.success && (typeof data.isFollowing !== 'undefined')) {
			$(button).html(data.isFollowing ? 'Unfollow' : 'Follow');
			$(button).blur();
			if(num!=null) $(num).html(data.followers);
			if(numStr!=null) $(numStr).html(data['followers-str']);
			console.log('follow()',data);
		}
		console.log('follow()',data);
	}, 'json');
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
	}, 'json');
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
	}, 'json');
}

function repost(id, caption) {
	// stars the meme with $id, expects:
	// id 		to be the meme's id
	// caption	to be the caption input box

	$.post('/ajax/repost.php', {id: id, caption: $(caption).val()}, function (data) {
		if(data.success) window.location.href = data.link;
		console.log('repost()',data);
	}, 'json');
}


function expand(elem) {
	// expand them [elem]ent

	$(elem).slideDown();
}

function htmlEncode(s) { // http://stackoverflow.com/a/784698
	var el = document.createElement("div");
	el.innerText = el.textContent = s;
	s = el.innerHTML;
	return s;
}