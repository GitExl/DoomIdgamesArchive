"""
Modified version of https://gist.github.com/eliben/5797351

lexer.py

A generic regex-based Lexer/tokenizer tool.
See the if __main__ section in the bottom for an example.

Eli Bendersky (eliben@gmail.com)

This code is in the public domain
Last modified: August 2010
"""

import re
from dataclasses import dataclass
from typing import Dict, List, Optional, Pattern, Tuple


Token = Tuple[str, str, int]


@dataclass(frozen=True)
class Rule:
    name: str
    regex: str
    skip: bool = False


class LexerError(Exception):
    """ Lexer error exception.

        pos:
            Position in the input line where the error occurred.
    """
    def __init__(self, message: str, position: Tuple[int, int]):
        super(Exception, self).__init__('Line {} column {}: {}'.format(position[0], position[1], message))


class Lexer(object):
    """ A simple regex-based lexer/tokenizer.

        See below for an example of usage.
    """

    def __init__(self, rules: List[Rule]):
        """ Create a lexer.

            rules:
                A list of rules. Each rule is a `regex, type`
                pair, where `regex` is the regular expression used
                to recognize the token and `type` is the type
                of the token to return when it's recognized.

            skip_whitespace:
                If True, whitespace (\\s+) will be skipped and not
                reported by the lexer. Otherwise, you have to
                specify your rules for whitespace, or it will be
                flagged as an error.

            single_line_comments:
                Defines a list of regular expressions for finding
                the start of single line comments in whitespace.
                Not used if skip_whitespace is False.

            multiline_comment:
                Defines a tuple of regular expression that comprise
                the start and end of a multiline comment in whitespace.
                Not used if skip_whitespace is False.
        """

        # All the regexes are concatenated into a single one
        # with named groups. Since the group names must be valid
        # Python identifiers, but the token types used by the
        # user are arbitrary strings, we auto-generate the group
        # names and map them to token types.
        idx = 1
        regex_parts = []
        self.group_rules: Dict[str, Rule] = {}
        for rule in rules:
            group_name = 'G{}'.format(idx)
            regex_parts.append('(?P<{}>{})'.format(group_name, rule.regex))
            self.group_rules[group_name] = rule
            idx += 1

        self.regex: Pattern = re.compile('|'.join(regex_parts))
        self.re_whitespace: Pattern = re.compile(r'[\n\r\s]+')

        self.buf: Optional[str] = None
        self.buf_len: int = 0
        self.pos: int = 0

    def input(self, buf: str):
        """ Initialize the lexer with a buffer as input.
        """
        self.buf = buf
        self.buf_len = len(buf)
        self.pos = 0

    def require_token(self, token_type: str) -> str:
        token = self.get_token()
        if token is None:
            raise LexerError('Expected "{}" token but reached end of file.'.format(token_type), self._split_position(token[2]))
        if token[0] != token_type:
            raise LexerError('Expected "{}" token, got "{}".'.format(token_type, token[0]), self._split_position(token[2]))

        return token[1]

    def get_token(self) -> Optional[Token]:
        """ Return the next token (a Token object) found in the
            input buffer. None is returned if the end of the
            buffer was reached.
            In case of a lexing error (the current chunk of the
            buffer matches no rule), a LexerError is raised with
            the position of the error.
        """

        while True:
            if self.pos >= self.buf_len:
                return None

            m = self.regex.match(self.buf, self.pos)
            if not m:
                break

            self.pos = m.end()
            group_name = m.lastgroup
            rule = self.group_rules[group_name]
            if not rule.skip:
                return rule.name, m.group(group_name), m.pos

        # If we're here, no rule matched.
        raise LexerError('Invalid token', self._split_position(self.pos))

    def _split_position(self, pos: int) -> (int, int):
        text_part = self.buf[:pos]

        line = text_part.count('\n') + 1
        line_start = text_part.rfind('\n')
        if line_start > 0:
            return line, pos - line_start + 1

        return line, pos + 1
