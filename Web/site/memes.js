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
		else console.log(data);
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
			console.log('like()',data);
		}
		else console.log(data);
	}, 'json');
}