function follow(button, id) {
	// follows the user with $id
	// expects button to be the html button that triggered this event
	// expects $id to be the user's id

	$.post('/ajax/follow.php', {id: id}, function (data) {
		if(data.success && (typeof data.isFollowing !== 'undefined'))
			$(button).html(data.isFollowing ? 'Unfollow' : 'Follow');
		else console.log(data);
	}, 'json');
}