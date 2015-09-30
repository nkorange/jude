program Cradle;

{ Constant Declarations }

const TAB = ^|;

{ Variable Declarations }

var Look: char;			{ Lookahead Character }

{ Read New Character From Input Stream }

procedure GetChar;
begin
	Read(Look);
end;

{ Report An Error }

procedure Error(s: string);
begin
	WriteLn;
	WriteLn(^G, 'Error: ', s, '.');
end;

{ Report Error and Halt }

procedure Abort(s: string);
begin
	Error(s);
	Halt;
end;

{ Report What Was Expected }

procedure Expected(s: string);
begin
	Abort(s + ' Expected');
end;

{ Match a Specific Input Character }

procedure Match(x: char);
begin
	if Look = x then GetChar
	else Expected('''' + x + '''');
end;

{ Recognize an Alpha Character }

function IsAlpha(c: char): boolean;
begin
	IsAlpha := upcase(c) in ['A'..'Z'];
end;

{ Recognize a Decimal Digit }

function IsDigit(c: char): boolean;
begin
	IsDigit := c in ['0'..'9'];
end;

{ Get an Identifier }

function GetName: char;
begin
	if not IsAlpha(Look) then Expected('Name');
	GetName := UpCase(Look);
	GetChar;
end;

{ Get a Number }

function GetNum: char;
begin
	if not IsDigit(Look) then Expected('Integer');
	GetNum := Look;
	GetChar;
end;

{ Output a String with Tab }

procedure Emit(s: string);
begin
	Write(TAB, s);
end;

{ Output a String with Tab and CRLF }

procedure EmitLn(s: string);
begin
	Emit(s);
	WriteLn;
end;

{ Parse and Translate a Math Expression }

procedure Expression;
begin
	Term;
	EmitLn('MOVE D0,D1');
	case Look of
		'+': Add;
		'-': Substract;
	else Expected('Addop');
	end;
end;

{ Recognize and Translate an Add }

{ Initialize }

procedure Init;
begin
	GetChar;
end;

{ Main Program }

begin
	Init;
	Expression;
end.









