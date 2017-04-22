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
			console.log('Follow()',data);
		}
		else console.log(data);
	}, 'json');
}