<?php
	session_start();
	require_once dirname(__FILE__).'/secure.php';

	function valid($field, $text) {
		// verify the text is valid to be inserted
		global $dbh;

		list($table, $column) = explode('.',$field);

		$validation = array( // friendly name (null if the same), min (if different), max (if different)
			'user.username' => array(null, 3, 20),
			'user.password' => array(null, null, 50),
			'user.email' => array(null, null, null),
			'user.firstName' => array('first name', null, null),
			'user.surname' => array(null, null, null),
			'user.password' => array(null, 8, 50)
		);

		try {
			$sql = "SELECT character_maximum_length as len, IS_NULLABLE as n   
					FROM information_schema.columns  
					WHERE table_name = '$table'
					AND column_name = '$column'";

			$sth = $dbh->prepare($sql);

			$sth->execute();

			$attr = $sth->fetch(PDO::FETCH_OBJ);

			if(isset($validation[$field])) {
				$friendly = is_null($validation[$field][0]) ? $column : $validation[$field][0];
				$min = is_null($validation[$field][1]) ? 0 : $validation[$field][1];
				$max = is_null($validation[$field][2]) ? $attr->len : $validation[$field][2];
			}
			else { // it hasn't been set up in the validation array
				$friendly = $column;
				$min = 0;
				$max = $attr->len;
			}

			if(strlen($text) > $max)
				return "The $friendly must be less than $max characters.";

			if(empty(ltrim($text)) && ($attr->n == "NO"))
				return "The $friendly can't be left blank.";

			if(strlen($text) < $min)
				return "The $friendly must be more than $min characters.";

			return true;
		}
		catch (PDOException $e) {
			echo $e->getMessage();
		}
	}
?>