function follow(button, id) {
	// follows the user with $id
	// expects button to be the html button that triggered this event
	// expects $id to be the user's id

	console.log('hello');

	$.post('/ajax/follow.php', {id: id}, function (data) {
		console.log(data.success);
		console.log(data.isFollowing);
	}, 'json');
}