// Copyright 2019 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.

use std::error::Error;
use std::sync::TryLockError;
use std::{fmt, result};

use j4rs;

pub type Result<T> = result::Result<T, RklAndroidError>;

#[derive(Debug)]
pub struct RklAndroidError {
    description: String,
}

impl fmt::Display for RklAndroidError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.description)
    }
}

impl Error for RklAndroidError {
    fn description(&self) -> &str {
        self.description.as_str()
    }
}

impl From<j4rs::errors::J4RsError> for RklAndroidError {
    fn from(err: j4rs::errors::J4RsError) -> RklAndroidError {
        RklAndroidError {
            description: format!("{:?}", err),
        }
    }
}

impl<T> From<TryLockError<T>> for RklAndroidError {
    fn from(err: TryLockError<T>) -> RklAndroidError {
        RklAndroidError {
            description: format!("{:?}", err),
        }
    }
}
